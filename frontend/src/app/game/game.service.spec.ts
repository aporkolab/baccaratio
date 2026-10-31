import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';

import { GameService } from './game.service';
import { environment } from '../../environments/environment';
import { GameResponse } from '../model/dto/game-response.dto';

describe('GameService', () => {
  let service: GameService;
  let httpMock: HttpTestingController;
  const api = environment.apiUrl;

  const gameResponse: GameResponse = {
    result: 'Player',
    playerCards: [{ suit: 'Hearts', value: '9', points: 9 }, { suit: 'Spades', value: 'K', points: 0 }],
    bankerCards: [{ suit: 'Clubs', value: '2', points: 2 }, { suit: 'Clubs', value: '3', points: 3 }],
    playerChips: 1100,
    message: 'Round played successfully.',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GameService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GameService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('places a bet and publishes the returned balance', async () => {
    const result = firstValueFrom(service.placeBet('playerPair', 25));

    const req = httpMock.expectOne(`${api}/baccarat/bet/playerPair/25`);
    expect(req.request.method).toBe('POST');
    req.flush({ message: 'Bet placed', chips: 1000 });

    expect((await result).chips).toBe(1000);
    expect(await firstValueFrom(service.balance$)).toBe(1000);
  });

  it('plays a round and updates the balance without revealing all cards at once', async () => {
    const result = firstValueFrom(service.playGame());
    httpMock.expectOne(`${api}/baccarat/play`).flush(gameResponse);

    expect((await result).result).toBe('Player');
    expect(await firstValueFrom(service.balance$)).toBe(1100);
    expect(await firstValueFrom(service.playerCards$)).toEqual([]);
  });

  it('loads the game state into the card and balance streams', async () => {
    const result = firstValueFrom(service.getGameState());
    httpMock.expectOne(`${api}/baccarat/state`).flush(gameResponse);
    await result;

    expect(await firstValueFrom(service.playerCards$)).toEqual(gameResponse.playerCards);
    expect(await firstValueFrom(service.bankerCards$)).toEqual(gameResponse.bankerCards);
    expect(await firstValueFrom(service.balance$)).toBe(1100);
  });

  it('resets the game', async () => {
    const result = firstValueFrom(service.resetGame());
    const req = httpMock.expectOne(`${api}/baccarat/reset`);
    expect(req.request.method).toBe('POST');
    req.flush({ ...gameResponse, playerCards: [], bankerCards: [], playerChips: 1000 });
    await result;

    expect(await firstValueFrom(service.balance$)).toBe(1000);
    expect(await firstValueFrom(service.playerCards$)).toEqual([]);
  });

  it('passes the backend message through for client errors', async () => {
    const result = firstValueFrom(service.placeBet('player', 5000));
    httpMock.expectOne(`${api}/baccarat/bet/player/5000`)
      .flush({ message: 'Insufficient chips for this bet.' }, { status: 400, statusText: 'Bad Request' });

    await expectAsync(result).toBeRejectedWithError('Insufficient chips for this bet.');
  });

  it('hides server error details behind a generic message', async () => {
    const result = firstValueFrom(service.playGame());
    httpMock.expectOne(`${api}/baccarat/play`)
      .flush({ message: 'NullPointerException at ...' }, { status: 500, statusText: 'Server Error' });

    await expectAsync(result).toBeRejectedWithError('Something bad happened; please try again later.');
  });

  it('keeps a bet history', async () => {
    service.addBetToHistory({ type: 'tie', amount: 5, result: 'Banker' });
    service.addBetToHistory({ type: 'player', amount: 10, result: 'Player' });

    expect((await firstValueFrom(service.betHistory$)).map(bet => bet.type)).toEqual(['tie', 'player']);
  });
});
