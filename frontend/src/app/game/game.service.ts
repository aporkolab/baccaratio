import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, map, catchError, switchMap, finalize } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Card } from '../model/card';
import { Player } from '../model/player';
import { GameResponse } from '../model/dto/game-response.dto';
import { BetResponse } from '../model/dto/bet-response.dto';
import { PlayerDTO } from '../model/dto/player.dto';
import { Bet } from '../model/bet';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private http = inject(HttpClient);

  apiUrl: string = environment.apiUrl;
  private playerCardsSubject = new BehaviorSubject<Card[]>([]);
  private bankerCardsSubject = new BehaviorSubject<Card[]>([]);
  private balanceSubject = new BehaviorSubject<number>(1000);
  private betHistorySubject = new BehaviorSubject<Bet[]>([]);
  balance$ = this.balanceSubject.asObservable();
  betHistory$ = this.betHistorySubject.asObservable();
  isLoading: boolean = false;

  playerCards$ = this.playerCardsSubject.asObservable();
  bankerCards$ = this.bankerCardsSubject.asObservable();

  addBetToHistory(bet: Bet): void {
    const currentHistory = this.betHistorySubject.value;
    this.betHistorySubject.next([...currentHistory, bet]);
  }

  placeBet(type: string, amount: number): Observable<BetResponse> {
    return this.http
      .post<BetResponse>(`${this.apiUrl}/baccarat/bet/${type}/${amount}`, {})
      .pipe(
        tap((response) => this.updateBalance(response.chips)),
        catchError(this.handleError),
      );
  }

  playGame(): Observable<GameResponse> {
    return this.http.get<GameResponse>(`${this.apiUrl}/baccarat/play`).pipe(
      tap((response) => {
        this.updateCards(response.playerCards, response.bankerCards);
        this.updateBalance(response.playerChips);
      }),
      catchError(this.handleError),
    );
  }

  getGameState(): Observable<GameResponse> {
    return this.http.get<GameResponse>(`${this.apiUrl}/baccarat/state`).pipe(
      tap((response) => {
        this.updateCards(response.playerCards, response.bankerCards);
        this.updateBalance(response.playerChips);
      }),
      catchError(this.handleError),
    );
  }

  resetGame(): Observable<GameResponse> {
    return this.http
      .post<GameResponse>(`${this.apiUrl}/baccarat/reset`, {})
      .pipe(
        tap((response) => {
          this.updateCards(response.playerCards, response.bankerCards);
          this.updateBalance(response.playerChips);
        }),
        catchError(this.handleError),
      );
  }

  getPlayer(): Observable<PlayerDTO> {
    // This endpoint might be deprecated in favor of getGameState, but we'll keep it for now
    return this.http.get<PlayerDTO>(`${this.apiUrl}/baccarat/player`).pipe(
      tap((response) => this.updateBalance(response.chips)),
      catchError(this.handleError),
    );
  }

  public updateBalance(newBalance: number): void {
    this.balanceSubject.next(newBalance);
  }

  public updateCards(playerCards: Card[], bankerCards: Card[]): void {
    this.playerCardsSubject.next(playerCards);
    this.bankerCardsSubject.next(bankerCards);
  }

  public updatePlayerCards(card: Card): void {
    const currentCards = this.playerCardsSubject.value;
    this.playerCardsSubject.next([...currentCards, card]);
  }

  public updateBankerCards(card: Card): void {
    const currentCards = this.bankerCardsSubject.value;
    this.bankerCardsSubject.next([...currentCards, card]);
  }

  private handleError(error: HttpErrorResponse) {
    console.error(
      'An error occurred:',
      error.error.message || error.statusText,
    );
    return throwError(
      () => new Error('Something bad happened; please try again later.'),
    );
  }
}
