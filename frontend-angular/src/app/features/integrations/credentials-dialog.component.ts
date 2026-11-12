import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { ApiKeyResponse, Credential, Integration } from '../../core/models';
import { ConfirmationService } from '../../shared/dialogs/confirmation.service';
import { SecretDialogComponent } from '../../shared/dialogs/secret-dialog.component';
import { CredentialFormComponent } from './components/credential-form.component';
import { CredentialListComponent } from './components/credential-list.component';

@Component({
  standalone: true,
  imports: [CredentialFormComponent, CredentialListComponent, MatButtonModule, MatDialogModule, MatIconModule, MatProgressBarModule, MatSnackBarModule],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <span><small>INTEGRAÇÃO</small>Credenciais</span>
      <button mat-icon-button type="button" mat-dialog-close aria-label="Fechar"><mat-icon>close</mat-icon></button>
    </h2>
    <mat-dialog-content class="dialog-content">
      <p class="description">{{ integration.name }} <code>{{ integration.basePath }}</code></p>
      @if (error()) { <div class="error-banner" role="alert">{{ error() }}</div> }
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      @if (editing()) {
        <ihub-credential-form
          [credential]="editing()"
          [saving]="saving()"
          [error]="formError()"
          (saved)="save($event)"
          (cancel)="cancelForm()"
        />
      } @else {
        <div class="toolbar">
          <div><h3>Chaves de consumo</h3><p>Use uma credencial por sistema consumidor.</p></div>
          <button mat-flat-button color="primary" type="button" [disabled]="loading()" (click)="newCredential()"><mat-icon>add</mat-icon>Nova credencial</button>
        </div>
        <ihub-credential-list
          [credentials]="items()"
          (viewKey)="viewKey($event)"
          (edit)="editCredential($event)"
          (toggle)="toggleCredential($event)"
          (regenerate)="regenerateKey($event)"
        />
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button type="button" mat-dialog-close>Fechar</button></mat-dialog-actions>
  `,
  styles: [`
    .dialog-title{display:flex;align-items:center;justify-content:space-between;gap:12px;padding-right:8px}.dialog-title span{display:grid;gap:4px}.dialog-title small{color:#667085;font-size:.58rem;letter-spacing:.13em}.dialog-content{min-height:220px}.description{display:flex;align-items:center;gap:9px;margin:0 0 16px;color:#475467;font-size:.82rem}.description code{padding:4px 7px;border-radius:5px;background:#f2f4f7;font-size:.72rem}.toolbar{display:flex;align-items:center;justify-content:space-between;gap:14px;padding:10px 0 16px}.toolbar h3{margin:0;font-size:1rem}.toolbar p{margin:4px 0 0;color:#667085;font-size:.78rem}.error-banner{margin:8px 0 12px;padding:10px 12px;border-radius:8px;background:#fef3f2;color:#b42318;font-size:.82rem}@media(max-width:560px){.toolbar{align-items:flex-start;flex-direction:column}.toolbar button{width:100%}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CredentialsDialogComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly confirmation = inject(ConfirmationService);
  private readonly snack = inject(MatSnackBar);
  readonly items = signal<Credential[]>([]);
  readonly editing = signal<Credential | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly formError = signal('');

  readonly integration = inject<Integration>(MAT_DIALOG_DATA);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.credentials(this.integration.id!).subscribe({
      next: items => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: error => {
        this.error.set(error.error?.message || 'Não foi possível carregar as credenciais.');
        this.loading.set(false);
      },
    });
  }

  newCredential(): void {
    this.error.set('');
    this.formError.set('');
    this.editing.set({} as Credential);
  }

  editCredential(credential: Credential): void {
    this.error.set('');
    this.formError.set('');
    this.editing.set(credential);
  }

  cancelForm(): void {
    this.editing.set(null);
    this.formError.set('');
  }

  save(name: string): void {
    const current = this.editing();
    if (!current || this.saving()) return;
    this.saving.set(true);
    this.formError.set('');
    const request: Observable<Credential | ApiKeyResponse> = current.id
      ? this.api.updateCredential(this.integration.id!, current.id, name)
      : this.api.createCredential(this.integration.id!, name);

    request.subscribe({
      next: result => {
        this.saving.set(false);
        this.editing.set(null);
        if ('apiKey' in result) this.showKey(result.apiKey, 'API Key criada');
        this.snack.open('Credencial salva com sucesso.', 'Fechar', { duration: 2200 });
        this.load();
      },
      error: error => {
        this.saving.set(false);
        this.formError.set(error.error?.message || 'Não foi possível salvar a credencial.');
      },
    });
  }

  viewKey(credential: Credential): void {
    this.api.credentialKey(this.integration.id!, credential.id).subscribe({
      next: result => this.showKey(result.apiKey, `API Key · ${credential.name}`),
      error: error => this.error.set(error.error?.message || 'Não foi possível consultar a API Key.'),
    });
  }

  regenerateKey(credential: Credential): void {
    this.confirmation.confirm({
      title: 'Regenerar API Key?',
      message: `A chave atual de “${credential.name}” deixará de funcionar imediatamente.`,
      confirmLabel: 'Regenerar chave',
      destructive: true,
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.api.credentialKey(this.integration.id!, credential.id, true).subscribe({
        next: result => this.showKey(result.apiKey, `API Key regenerada · ${credential.name}`),
        error: error => this.error.set(error.error?.message || 'Não foi possível regenerar a API Key.'),
      });
    });
  }

  toggleCredential(credential: Credential): void {
    const active = credential.active !== 'S';
    this.confirmation.confirm({
      title: active ? 'Ativar credencial?' : 'Desativar credencial?',
      message: active
        ? `A credencial “${credential.name}” poderá consumir os endpoints desta integração.`
        : `A credencial “${credential.name}” perderá acesso aos endpoints da integração.`,
      confirmLabel: active ? 'Ativar credencial' : 'Desativar credencial',
      destructive: !active,
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.api.setCredentialActive(this.integration.id!, credential.id, active).subscribe({
        next: () => {
          this.snack.open(active ? 'Credencial ativada.' : 'Credencial desativada.', 'Fechar', { duration: 1800 });
          this.load();
        },
        error: error => this.error.set(error.error?.message || 'Não foi possível alterar a credencial.'),
      });
    });
  }

  private showKey(secret: string, title: string): void {
    this.dialog.open(SecretDialogComponent, {
      width: 'min(560px, 94vw)',
      data: {
        title,
        description: 'Esta chave autentica chamadas aos endpoints desta integração.',
        warning: 'Trate a API Key como uma senha e envie-a no header X-API-Key. Não a coloque na URL.',
        secret,
        label: 'API Key',
      },
    });
  }
}
