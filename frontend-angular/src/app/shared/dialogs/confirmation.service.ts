import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { ConfirmationDialogComponent, ConfirmationDialogData } from './confirmation-dialog.component';

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
  private readonly dialog = inject(MatDialog);

  confirm(data: ConfirmationDialogData): Observable<boolean> {
    return this.dialog.open(ConfirmationDialogComponent, {
      width: 'min(460px, 94vw)',
      data,
      autoFocus: 'dialog',
      restoreFocus: true,
    }).afterClosed().pipe(map(result => result === true));
  }
}
