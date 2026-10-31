import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { BetPanelComponent } from './bet-panel.component';
import { GameService } from '../game.service';
import { ToastService } from '../../service/toast.service';
import { GameResponse } from '../../model/dto/game-response.dto';

describe('BetPanelComponent', () => {
  let component: BetPanelComponent;
  let fixture: ComponentFixture<BetPanelComponent>;
  let gameService: jasmine.SpyObj<GameService>;
  let toastr: jasmine.SpyObj<ToastService>;

  const round: GameResponse = {
    result: 'Banker',
    playerCards: [{ suit: 'Hearts', value: '2', points: 2 }, { suit: 'Hearts', value: '5', points: 5 }],
    bankerCards: [{ suit: 'Clubs', value: '4', points: 4 }, { suit: 'Clubs', value: '4', points: 4 }],
    playerChips: 900,
    message: 'Round played successfully.',
  };

  beforeEach(async () => {
    gameService = jasmine.createSpyObj<GameService>('GameService',
      ['placeBet', 'playGame', 'addBetToHistory', 'updateCards', 'updatePlayerCards', 'updateBankerCards']);
    toastr = jasmine.createSpyObj<ToastService>('ToastService', ['success', 'error', 'info', 'warning']);

    await TestBed.configureTestingModule({
      imports: [BetPanelComponent],
      providers: [
        { provide: GameService, useValue: gameService },
        { provide: ToastService, useValue: toastr },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BetPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('builds the bet from chips and supports undo and double', () => {
    component.selectChip(component.chips[2]); // 25
    component.addToBet();
    component.selectChip(component.chips[1]); // 5
    component.addToBet();
    expect(component.currentBetAmount).toBe(30);

    component.undoLastBet();
    expect(component.currentBetAmount).toBe(25);

    component.doubleBet();
    expect(component.currentBetAmount).toBe(50);
  });

  it('refuses to bet without an amount', async () => {
    await component.placeBet('player');

    expect(toastr.warning).toHaveBeenCalled();
    expect(gameService.placeBet).not.toHaveBeenCalled();
  });

  it('places the bet, plays the round and deals the cards one by one', fakeAsync(() => {
    gameService.placeBet.and.returnValue(of({ message: 'ok', chips: 1000 }));
    gameService.playGame.and.returnValue(of(round));
    component.selectChip(component.chips[3]); // 100
    component.addToBet();

    void component.placeBet('banker');
    tick();

    expect(gameService.placeBet).toHaveBeenCalledWith('banker', 100);
    expect(gameService.addBetToHistory).toHaveBeenCalledWith({ type: 'banker', amount: 100, result: 'Banker' });
    expect(gameService.updateCards).toHaveBeenCalledWith([], []);
    expect(component.loading).toBeFalse();

    tick(1000);
    expect(gameService.updatePlayerCards).toHaveBeenCalledTimes(1);
    tick(3000);
    expect(gameService.updatePlayerCards).toHaveBeenCalledTimes(2);
    expect(gameService.updateBankerCards).toHaveBeenCalledTimes(2);

    tick(3000);
    expect(component.currentBetAmount).toBe(0);
    expect(toastr.info).toHaveBeenCalledWith('Round finished. Winner: Banker', 'Round Over');
  }));

  it('shows the error message when the bet fails', fakeAsync(() => {
    gameService.placeBet.and.returnValue(throwError(() => new Error('Insufficient chips for this bet.')));
    component.addToBet();

    void component.placeBet('player');
    tick();

    expect(toastr.error).toHaveBeenCalledWith('Insufficient chips for this bet.', 'Error');
    expect(gameService.playGame).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  }));

  it('stops dealing cards when destroyed', fakeAsync(() => {
    component.animateCards(round.playerCards, round.bankerCards);
    fixture.destroy();
    tick(10000);

    expect(gameService.updatePlayerCards).not.toHaveBeenCalled();
  }));
});
