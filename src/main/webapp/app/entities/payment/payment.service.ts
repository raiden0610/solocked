import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {JhiDateUtils} from 'ng-jhipster';
import {SERVER_API_URL} from '../../app.constants';

import {Payment} from './payment.model';
import {createRequestOption} from '../../shared';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {HttpClient, HttpResponse} from '@angular/common/http';

export type EntityResponseType = HttpResponse<Payment>;

@Injectable()
export class PaymentService {

    private resourceUrl =  SERVER_API_URL + 'api/payments';

    payment$: BehaviorSubject<Payment>;

    private _dataStore: {
        payment: Payment
    };

    constructor(private http: HttpClient, private dateUtils: JhiDateUtils) {
        this._dataStore = {payment: new Payment()};
        this.payment$ = new BehaviorSubject<Payment>(this._dataStore.payment);
    }

    create(payment: Payment): Observable<EntityResponseType> {
        const copy = this.convert(payment);
        return this.http.post<Payment>(this.resourceUrl, copy, {observe: 'response'})
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    update(payment: Payment): Observable<EntityResponseType> {
        const copy = this.convert(payment);
        return this.http.put<Payment>(this.resourceUrl, copy, {observe: 'response'})
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    find(id: number): Observable<EntityResponseType> {
        return this.http.get<Payment>(`${this.resourceUrl}/${id}`, {observe: 'response'})
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    query(req?: any): Observable<HttpResponse<Payment[]>> {
        const options = createRequestOption(req);
        return this.http.get<Payment[]>(this.resourceUrl, {params: options, observe: 'response'})
            .map((res: HttpResponse<Payment[]>) => this.convertArrayResponse(res));
    }

    delete(id: number): Observable<HttpResponse<any>> {
        return this.http.delete<any>(`${this.resourceUrl}/${id}`, {observe: 'response'});
    }

    private convertResponse(res: EntityResponseType): EntityResponseType {
        const body: Payment = this.convertItemFromServer(res.body);
        return res.clone({body});
    }

    private convertArrayResponse(res: HttpResponse<Payment[]>): HttpResponse<Payment[]> {
        const jsonResponse: Payment[] = res.body;
        const body: Payment[] = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            body.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return res.clone({body});
    }

    getPaymentByLogin() {
        this.http.get(this.resourceUrl + '-by-login')
            .map((res: HttpResponse<Payment>) => res.body)
            .subscribe((payment: Payment) => {
                this._dataStore.payment = payment;
                this.payment$.next(this._dataStore.payment);
            });
    }

    /**
     * Convert a returned JSON object to Payment.
     */
    private convertItemFromServer(payment: Payment): Payment {
        const copy: Payment = Object.assign({}, payment);
        copy.subscriptionDate = this.dateUtils
            .convertLocalDateFromServer(payment.subscriptionDate);
        return copy;
    }

    /**
     * Convert a Payment to a JSON which can be sent to the server.
     */
    private convert(payment: Payment): Payment {
        const copy: Payment = Object.assign({}, payment);
        copy.subscriptionDate = this.dateUtils
            .convertLocalDateToServer(payment.subscriptionDate);
        return copy;
    }
}
