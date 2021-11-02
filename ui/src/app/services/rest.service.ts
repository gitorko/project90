import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Item} from '../models/item';

@Injectable({
  providedIn: 'root'
})
export class RestService {

  constructor(private http: HttpClient) {
  }

  public getUser(): Observable<string> {
    return this.http.get<string>('/api/user', {responseType: 'text' as 'json'});
  }

  public getCartItems(username: string | null): Observable<Item[]> {
    return this.http.get<Item[]>('/api/cart/items/' + username);
  }

  public getFreeItemCount(): Observable<number> {
    return this.http.get<number>('/api/items/count');
  }

  public addCartItem(username: string | null): Observable<any> {
    return this.http.get('/api/cart/' + username);
  }

  public getAuditToken(token: string | null): Observable<any> {
    return this.http.get('/api/audit/' + token);
  }

  public deleteCartItem(username: string | null, id: number): Observable<any> {
    return this.http.delete('/api/cart/' + username + '/' + id);
  }

}
