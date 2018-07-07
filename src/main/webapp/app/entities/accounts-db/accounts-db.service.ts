import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { SERVER_API_URL } from '../../app.constants';

import { AccountsDB } from './accounts-db.model';
import { createRequestOption } from '../../shared';

export type EntityResponseType = HttpResponse<AccountsDB>;

@Injectable()
export class AccountsDBService {

    private resourceUrl =  SERVER_API_URL + 'api/accounts-dbs';

    constructor(private http: HttpClient) { }

    create(accountsDB: AccountsDB): Observable<EntityResponseType> {
        const copy = this.convert(accountsDB);
        return this.http.post<AccountsDB>(this.resourceUrl, copy, { observe: 'response' })
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    update(accountsDB: AccountsDB): Observable<EntityResponseType> {
        const copy = this.convert(accountsDB);
        return this.http.put<AccountsDB>(this.resourceUrl, copy, { observe: 'response' })
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    find(id: number): Observable<EntityResponseType> {
        return this.http.get<AccountsDB>(`${this.resourceUrl}/${id}`, { observe: 'response'})
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    query(req?: any): Observable<HttpResponse<AccountsDB[]>> {
        const options = createRequestOption(req);
        return this.http.get<AccountsDB[]>(this.resourceUrl, { params: options, observe: 'response' })
            .map((res: HttpResponse<AccountsDB[]>) => this.convertArrayResponse(res));
    }

    delete(id: number): Observable<HttpResponse<any>> {
        return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response'});
    }

    private convertResponse(res: EntityResponseType): EntityResponseType {
        const body: AccountsDB = this.convertItemFromServer(res.body);
        return res.clone({body});
    }

    private convertArrayResponse(res: HttpResponse<AccountsDB[]>): HttpResponse<AccountsDB[]> {
        const jsonResponse: AccountsDB[] = res.body;
        const body: AccountsDB[] = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            body.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return res.clone({body});
    }

    /**
     * Convert a returned JSON object to AccountsDB.
     */
    private convertItemFromServer(accountsDB: AccountsDB): AccountsDB {
        const copy: AccountsDB = Object.assign({}, accountsDB);
        return copy;
    }

    /**
     * Convert a AccountsDB to a JSON which can be sent to the server.
     */
    private convert(accountsDB: AccountsDB): AccountsDB {
        const copy: AccountsDB = Object.assign({}, accountsDB);
        return copy;
    }
}
