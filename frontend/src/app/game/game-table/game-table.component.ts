import { Component, DestroyRef, OnInit, ViewChild, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { trigger, transition, query, style, stagger, animate } from '@angular/animations';
import { Card } from '../../model/card';
import { GameService } from '../game.service';
import { BetPanelComponent } from '../bet-panel/bet-panel.component';
import { PlayerStatusComponent } from '../player-status/player-status.component';
import { BetHistoryComponent } from '../bet-history/bet-history.component';

const CARD_NAMES: Readonly<Record<string, string>> = { a: 'ace', k: 'king', q: 'queen', j: 'jack' };

@Component({
  selector: 'app-game-table',
  templateUrl: './game-table.component.html',
  styleUrls: ['./game-table.component.scss'],
  standalone: true,
  imports: [PlayerStatusComponent, BetPanelComponent, BetHistoryComponent],
  animations: [
    trigger('cardAnimation', [
      transition('* => *', [
        query(
          ':enter',
          [
            style({ opacity: 0, transform: 'translateY(-50px)' }),
            stagger(
              '150ms',
              animate(
                '500ms cubic-bezier(0.35, 0, 0.25, 1)',
                style({ opacity: 1, transform: 'none' }),
              ),
            ),
          ],
          { optional: true },
        ),
      ]),
    ]),
  ],
})
export class GameTableComponent implements OnInit {
  private readonly gameService = inject(GameService);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild(PlayerStatusComponent) playerStatusComponent?: PlayerStatusComponent;

  playerCards: Card[] = [];
  bankerCards: Card[] = [];
  isLoading = false;

  ngOnInit(): void {
    this.gameService.playerCards$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(cards => this.playerCards = cards);
    this.gameService.bankerCards$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(cards => this.bankerCards = cards);
  }

  handleBetAmountChange(amount: number): void {
    this.playerStatusComponent?.updateCurrentBetAmount(amount);
  }

  getCardImage(card: Card): string {
    if (!card || !card.value || !card.suit) {
      return 'assets/cards/back.png';
    }
    const value = card.value.toLowerCase();
    return `assets/cards/${CARD_NAMES[value] ?? value}_of_${card.suit.toLowerCase()}.png`;
  }
}
