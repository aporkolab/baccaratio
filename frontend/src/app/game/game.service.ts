import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Card } from '../model/card';
import { GameResponse } from '../model/dto/game-response.dto';
import { BetResponse } from '../model/dto/bet-response.dto';
import { Bet } from '../model/bet';

const GENERIC_ERROR = 'Something bad happened; please try again later.';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private readonly http = inject(HttpClient);

  readonly apiUrl: string = environment.apiUrl;
  private readonly playerCardsSubject = new BehaviorSubject<Card[]>([]);
  private readonly bankerCardsSubject = new BehaviorSubject<Card[]>([]);
  private readonly balanceSubject = new BehaviorSubject<number>(1000);
  private readonly betHistorySubject = new BehaviorSubject<Bet[]>([]);
  readonly balance$ = this.balanceSubject.asObservable();
  readonly betHistory$ = this.betHistorySubject.asObservable();
  readonly playerCards$ = this.playerCardsSubject.asObservable();
  readonly bankerCards$ = this.bankerCardsSubject.asObservable();

  addBetToHistory(bet: Bet): void {
    const currentHistory = this.betHistorySubject.value;
    this.betHistorySubject.next([...currentHistory, bet]);
  }

  placeBet(type: string, amount: number): Observable<BetResponse> {
    return this.http
      .post<BetResponse>(`${this.apiUrl}/baccarat/bet/${encodeURIComponent(type)}/${amount}`, {})
      .pipe(
        tap(response => this.updateBalance(response.chips)),
        catchError(error => this.handleError(error)),
      );
  }

  playGame(): Observable<GameResponse> {
    return this.http.get<GameResponse>(`${this.apiUrl}/baccarat/play`).pipe(
      tap(response => this.updateBalance(response.playerChips)),
      catchError(error => this.handleError(error)),
    );
  }

  getGameState(): Observable<GameResponse> {
    return this.http.get<GameResponse>(`${this.apiUrl}/baccarat/state`).pipe(
      tap(response => {
        this.updateCards(response.playerCards, response.bankerCards);
        this.updateBalance(response.playerChips);
      }),
      catchError(error => this.handleError(error)),
    );
  }

  resetGame(): Observable<GameResponse> {
    return this.http.post<GameResponse>(`${this.apiUrl}/baccarat/reset`, {}).pipe(
      tap(response => {
        this.updateCards(response.playerCards, response.bankerCards);
        this.updateBalance(response.playerChips);
      }),
      catchError(error => this.handleError(error)),
    );
  }

  updateBalance(newBalance: number): void {
    this.balanceSubject.next(newBalance);
  }

  updateCards(playerCards: Card[], bankerCards: Card[]): void {
    this.playerCardsSubject.next(playerCards);
    this.bankerCardsSubject.next(bankerCards);
  }

  updatePlayerCards(card: Card): void {
    this.playerCardsSubject.next([...this.playerCardsSubject.value, card]);
  }

  updateBankerCards(card: Card): void {
    this.bankerCardsSubject.next([...this.bankerCardsSubject.value, card]);
  }

  /** Surfaces the backend's error message (e.g. "Insufficient chips for this bet.") when there is one. */
  private handleError(error: HttpErrorResponse): Observable<never> {
    const serverMessage: unknown = error.error?.message;
    console.error('An error occurred:', serverMessage || error.statusText);
    const message = typeof serverMessage === 'string' && error.status > 0 && error.status < 500
      ? serverMessage
      : GENERIC_ERROR;
    return throwError(() => new Error(message));
  }
}
