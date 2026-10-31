import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Credentials {
  username: string;
  password: string;
}

export interface RegisteredUser {
  id: number;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'baccaratio_token';

  register(credentials: Credentials): Observable<RegisteredUser> {
    return this.http.post<RegisteredUser>(`${this.apiUrl}/register`, credentials);
  }

  login(credentials: Credentials): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => this.setToken(response.token))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /** True when a token is stored and, if it carries an `exp` claim, it has not expired yet. */
  isLoggedIn(): boolean {
    const token = this.getToken();
    if (token === null) {
      return false;
    }
    const expiresAt = AuthService.expirationOf(token);
    if (expiresAt !== null && expiresAt <= Date.now()) {
      this.logout();
      return false;
    }
    return true;
  }

  /** Reads the `exp` claim (milliseconds since epoch) without verifying the signature; the backend verifies. */
  private static expirationOf(token: string): number | null {
    const payload = token.split('.')[1];
    if (!payload) {
      return null;
    }
    try {
      const claims: unknown = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      const exp = (claims as { exp?: unknown }).exp;
      return typeof exp === 'number' ? exp * 1000 : null;
    } catch {
      return null;
    }
  }
}
