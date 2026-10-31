import { Component, DestroyRef, EventEmitter, Output, inject } from '@angular/core';
import { GameService } from '../game.service';
import { firstValueFrom } from 'rxjs';
import { ToastService } from '../../service/toast.service';
import { Card } from 'src/app/model/card';

interface Chip {
  value: number;
  label: string;
}
interface BetOption {
  type: string;
  label: string;
  odds: string;
}
@Component({
  selector: 'app-bet-panel',
  templateUrl: './bet-panel.component.html',
  styleUrls: ['./bet-panel.component.scss'],
  standalone: true,
  imports: [],
})
export class BetPanelComponent {
  private readonly gameService = inject(GameService);
  private readonly toastr = inject(ToastService);

  /** Pending card deals of the running animation; restarted by every new round. */
  private readonly dealTimers = new Set<ReturnType<typeof setTimeout>>();
  /** Pending round summaries. */
  private readonly summaryTimers = new Set<ReturnType<typeof setTimeout>>();

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.clearTimers(this.dealTimers);
      this.clearTimers(this.summaryTimers);
    });
  }

  chips: Chip[] = [
    { value: 1, label: '1' },
    { value: 5, label: '5' },
    { value: 25, label: '25' },
    { value: 100, label: '100' },
    { value: 500, label: '500' },
    { value: 2500, label: '2500' },
  ];
  betHistory: number[] = [];

  betOptions: BetOption[] = [
    { type: 'player', label: 'PLAYER', odds: '1:1' },
    { type: 'banker', label: 'BANKER', odds: '0.95:1' },
    { type: 'tie', label: 'TIE', odds: '8:1' },
    { type: 'playerPair', label: 'PLAYER PAIR', odds: '11:1' },
    { type: 'bankerPair', label: 'BANKER PAIR', odds: '11:1' },
    { type: 'eitherPair', label: 'EITHER PAIR', odds: '5:1' },
    { type: 'perfectPairOne', label: 'PERFECT PAIR', odds: '25:1' },
  ];

  selectedChip: Chip = this.chips[0];
  @Output() currentBetAmountChanged: EventEmitter<number> = new EventEmitter();
  currentBetAmount: number = 0;

  @Output() betPlaced: EventEmitter<{ type: string; amount: number }> =
    new EventEmitter();

  loading: boolean = false;

  selectChip(chip: Chip): void {
    this.selectedChip = chip;
  }

  addToBet(): void {
    this.currentBetAmount += this.selectedChip.value;
    this.betHistory.push(this.selectedChip.value);
    this.currentBetAmountChanged.emit(this.currentBetAmount);
  }

  doubleBet(): void {
    this.currentBetAmount *= 2;
    this.currentBetAmountChanged.emit(this.currentBetAmount);
  }

  undoLastBet(): void {
    if (this.betHistory.length > 0) {
      const lastBet = this.betHistory.pop();
      if (lastBet) {
        this.currentBetAmount = Math.max(0, this.currentBetAmount - lastBet);
        this.currentBetAmountChanged.emit(this.currentBetAmount);
      }
    }
  }

  async placeBet(betType: string): Promise<void> {
    if (this.currentBetAmount <= 0) {
      this.toastr.warning('Please select a bet amount before placing a bet.');
      return;
    }

    this.loading = true;
    try {
      await firstValueFrom(this.gameService.placeBet(betType, this.currentBetAmount));
      this.toastr.success(`Bet of ${this.currentBetAmount} on ${betType.toUpperCase()} accepted!`, 'Bet Placed');
      this.betPlaced.emit({ type: betType, amount: this.currentBetAmount });

      const gameResponse = await firstValueFrom(this.gameService.playGame());
      this.gameService.addBetToHistory({
        type: betType,
        amount: this.currentBetAmount,
        result: gameResponse.result,
      });

      this.schedule(this.summaryTimers, () => {
        this.currentBetAmount = 0;
        this.currentBetAmountChanged.emit(this.currentBetAmount);
        this.betHistory = [];
        // The game result is shown visually, a toast for it might be too much.
        this.toastr.info(`Round finished. Winner: ${gameResponse.result}`, 'Round Over');
      }, 7000);

      this.animateCards(gameResponse.playerCards, gameResponse.bankerCards);
    } catch (error: unknown) {
      console.error('Error during bet or game play:', error);
      const message = error instanceof Error && error.message ? error.message : 'An error occurred during the bet or game.';
      this.toastr.error(message, 'Error');
    } finally {
      this.loading = false;
    }
  }

  animateCards(playerCards: Card[], bankerCards: Card[]): void {
    this.clearTimers(this.dealTimers);
    this.gameService.updateCards([], []); // Clear the current cards
    // Deal the player's cards one by one, then the banker's, one second apart
    const deals = [
      ...playerCards.map(card => () => this.gameService.updatePlayerCards(card)),
      ...bankerCards.map(card => () => this.gameService.updateBankerCards(card)),
    ];
    deals.forEach((deal, index) => this.schedule(this.dealTimers, deal, (index + 1) * 1000));
  }

  private schedule(timers: Set<ReturnType<typeof setTimeout>>, action: () => void, delayMs: number): void {
    const timer = setTimeout(() => {
      timers.delete(timer);
      action();
    }, delayMs);
    timers.add(timer);
  }

  private clearTimers(timers: Set<ReturnType<typeof setTimeout>>): void {
    timers.forEach(timer => clearTimeout(timer));
    timers.clear();
  }
}
