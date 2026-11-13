import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Credential } from '../models/integration.models';

@Component({
  selector: 'ihub-credential-list',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './credential-list.component.html',
  styleUrl: './credential-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CredentialListComponent {
  @Input() credentials: Credential[] = [];
  @Output() viewKey = new EventEmitter<Credential>();
  @Output() edit = new EventEmitter<Credential>();
  @Output() toggle = new EventEmitter<Credential>();
  @Output() regenerate = new EventEmitter<Credential>();
}
