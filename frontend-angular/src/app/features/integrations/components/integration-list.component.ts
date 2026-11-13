import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Integration } from '../models/integration.models';

@Component({
  selector: 'ihub-integration-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule],
  templateUrl: './integration-list.component.html',
  styleUrl: './integration-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IntegrationListComponent {
  @Input() integrations: Integration[] = [];
  @Input() canEdit = false;
  @Input() loading = false;
  @Output() endpoints = new EventEmitter<Integration>();
  @Output() edit = new EventEmitter<Integration>();
  @Output() credentials = new EventEmitter<Integration>();
  @Output() generateLegacyKey = new EventEmitter<Integration>();
  @Output() remove = new EventEmitter<Integration>();
  readonly columns = ['integration', 'authentication', 'status', 'actions'];
}
