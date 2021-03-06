import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { JhiAlertService, JhiEventManager } from 'ng-jhipster';
import { filter, map } from 'rxjs/operators';

import { IPayment } from 'app/shared/model/payment.model';
import { PaymentService } from 'app/entities/payment/payment.service';
import { AccountService } from 'app/core';

@Component({
    selector: 'jhi-payment',
    templateUrl: './payment.component.html'
})
export class PaymentComponent implements OnInit, OnDestroy {
    payments: IPayment[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        protected paymentService: PaymentService,
        protected jhiAlertService: JhiAlertService,
        protected eventManager: JhiEventManager,
        protected accountService: AccountService
    ) {}

    loadAll() {
        this.paymentService
            .query()
            .pipe(
                filter((res: HttpResponse<IPayment[]>) => res.ok),
                map((res: HttpResponse<IPayment[]>) => res.body)
            )
            .subscribe(
                (res: IPayment[]) => {
                    this.payments = res;
                },
                (res: HttpErrorResponse) => this.onError(res.message)
            );
    }

    ngOnInit() {
        this.loadAll();
        this.accountService.identity().then(account => {
            this.currentAccount = account;
        });
        this.registerChangeInPayments();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: IPayment) {
        return item.id;
    }

    registerChangeInPayments() {
        this.eventSubscriber = this.eventManager.subscribe('paymentListModification', response => this.loadAll());
    }

    protected onError(errorMessage: string) {
        this.jhiAlertService.error(errorMessage, null, null);
    }
}
