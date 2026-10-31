import { ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { GameService } from '../game.service';
import { AuthService } from 'src/app/auth/auth.service';
import { ToastService } from '../../service/toast.service';

@Component({
  selector: 'app-player-status',
  templateUrl: './player-status.component.html',
  styleUrls: ['./player-status.component.scss'],
  standalone: true,
  imports: [DecimalPipe],
})
export class PlayerStatusComponent implements OnInit {
  private readonly gameService = inject(GameService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toastr = inject(ToastService);

  balance = 1000;
  playerName = 'Player';
  currentBetAmount = 0;

  ngOnInit(): void {
    // Fetch the initial game state; GameService pushes the balance into balance$
    this.gameService.getGameState()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ error: (error: Error) => this.toastr.error(error.message, 'Error') });

    this.gameService.balance$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(newBalance => {
        this.balance = newBalance;
        this.cdr.detectChanges();
      });
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }

  resetGame(): void {
    this.gameService.resetGame().subscribe({
      next: () => this.toastr.info('Your balance has been reset.', 'Game Reset'),
      error: (error: Error) => this.toastr.error(error.message, 'Error'),
    });
  }

  updateCurrentBetAmount(amount: number): void {
    this.currentBetAmount = amount;
    this.cdr.detectChanges();
  }
}
