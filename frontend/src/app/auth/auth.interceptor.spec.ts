import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    router.navigate.and.resolveTo(true);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    authService.setToken('jwt-token');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('baccaratio_token');
  });

  it('adds the bearer token to API requests', () => {
    http.get(`${environment.apiUrl}/baccarat/state`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/baccarat/state`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    req.flush({});
  });

  it('does not send the token to other hosts or to the auth endpoints', () => {
    http.get('https://fonts.example.com/font.css').subscribe();
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe();

    expect(httpMock.expectOne('https://fonts.example.com/font.css').request.headers.has('Authorization')).toBeFalse();
    expect(httpMock.expectOne(`${environment.apiUrl}/auth/login`).request.headers.has('Authorization')).toBeFalse();
    httpMock.match(() => true).forEach(req => req.flush({}));
  });

  it('logs out and redirects to the login page when the API answers 401', () => {
    let failed = false;
    http.get(`${environment.apiUrl}/baccarat/state`).subscribe({ error: () => failed = true });

    httpMock.expectOne(`${environment.apiUrl}/baccarat/state`)
      .flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(failed).toBeTrue();
    expect(authService.getToken()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('keeps the session on other errors', () => {
    http.get(`${environment.apiUrl}/baccarat/play`).subscribe({ error: () => undefined });

    httpMock.expectOne(`${environment.apiUrl}/baccarat/play`)
      .flush({ message: 'A bet must be placed' }, { status: 409, statusText: 'Conflict' });

    expect(authService.getToken()).toBe('jwt-token');
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
