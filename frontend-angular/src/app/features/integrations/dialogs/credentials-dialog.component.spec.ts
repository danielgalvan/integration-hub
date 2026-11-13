import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ConfirmationService } from '../../../shared/dialogs/confirmation.service';
import { SecretDialogComponent } from '../../../shared/dialogs/secret-dialog.component';
import { Credential, Integration } from '../models/integration.models';
import { IntegrationsApiService } from '../services/integrations-api.service';
import { CredentialsDialogComponent } from './credentials-dialog.component';

const integration: Integration = {
  id: 4,
  name: 'Pedidos',
  basePath: '/api/pedidos',
  active: 'S',
  authType: 'API_KEY',
};

const credential: Credential = {
  id: 12,
  integrationId: 4,
  name: 'Consumidor mobile',
  active: 'S',
};

describe('CredentialsDialogComponent', () => {
  it('loads and displays credentials for the integration passed to the dialog', () => {
    const dialog = { open: vi.fn() };
    const snack = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    const api = {
      credentials: vi.fn(() => of([credential])),
      createCredential: vi.fn(() => of({ apiKey: 'ihub_generated' })),
      updateCredential: vi.fn(),
      credentialKey: vi.fn(),
      setCredentialActive: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [CredentialsDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: integration },
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        { provide: IntegrationsApiService, useValue: api },
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snack },
        { provide: ConfirmationService, useValue: confirmation },
      ],
    });
    TestBed.overrideComponent(CredentialsDialogComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(CredentialsDialogComponent);
    fixture.detectChanges();

    expect(api.credentials).toHaveBeenCalledWith(integration.id);
    expect(fixture.nativeElement.textContent).toContain(integration.name);
    expect(fixture.nativeElement.textContent).toContain(credential.name);

    const component = fixture.componentInstance;
    component.newCredential();
    expect(component.editing()).toEqual({ name: '' });
    component.save('Consumer CRM');

    expect(api.createCredential).toHaveBeenCalledWith(integration.id, 'Consumer CRM');
    expect(component.editing()).toBeNull();
    expect(dialog.open).toHaveBeenCalledWith(
      SecretDialogComponent,
      expect.objectContaining({ data: expect.objectContaining({ secret: 'ihub_generated' }) }),
    );
    fixture.destroy();
  });
});
