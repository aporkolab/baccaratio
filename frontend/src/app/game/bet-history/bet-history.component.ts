import { Component, OnInit, inject } from '@angular/core';

import { GameService } from '../game.service';
import { Bet } from '../../model/bet';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-bet-history',
  standalone: true,
  imports: [],
  templateUrl: './bet-history.component.html',
  styleUrls: ['./bet-history.component.scss'],
})
export class BetHistoryComponent implements OnInit {
  private gameService = inject(GameService);

  betHistory: Bet[] = [];
  private subscription: Subscription = new Subscription();

  ngOnInit(): void {
    this.subscription.add(
      this.gameService.betHistory$.subscribe((history) => {
        this.betHistory = history;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
