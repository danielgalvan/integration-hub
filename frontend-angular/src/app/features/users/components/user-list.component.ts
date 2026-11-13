import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { User } from '../models/user.models';
import { UserRole } from '../../../core/models/common.models';

@Component({
  selector: 'ihub-user-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserListComponent {
  @Input() users: User[] = [];
  @Input() loading = false;
  @Output() edit = new EventEmitter<User>();
  @Output() resetPassword = new EventEmitter<User>();
  @Output() remove = new EventEmitter<User>();
  readonly columns = ['username', 'name', 'email', 'role', 'status', 'password', 'actions'];
  roleLabel(type: UserRole): string { return ({ A: 'Administrador', C: 'Criador', U: 'Consumidor' })[type]; }
}
