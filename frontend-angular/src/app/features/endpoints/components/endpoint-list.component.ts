import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Endpoint } from '../models/endpoint.models';

@Component({
  selector: 'ihub-endpoint-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule],
  templateUrl: './endpoint-list.component.html',
  styleUrl: './endpoint-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointListComponent {
  @Input() endpoints: Endpoint[] = [];
  @Input() canEdit = false;
  @Input() loading = false;
  @Output() test = new EventEmitter<Endpoint>();
  @Output() edit = new EventEmitter<Endpoint>();
  @Output() remove = new EventEmitter<Endpoint>();
  readonly columns = ['endpoint', 'parameters', 'status', 'actions'];
}
