import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { IPayment, Payment } from 'app/shared/model/payment.model';
import { PaymentService } from './payment.service';
import { PaymentComponent } from './payment.component';
import { PaymentDetailComponent } from './payment-detail.component';
import { PaymentUpdateComponent } from './payment-update.component';
import { PaymentDeletePopupComponent } from './payment-delete-dialog.component';

@Injectable({ providedIn: 'root' })
export class PaymentResolve implements Resolve<IPayment> {
    constructor(private service: PaymentService) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IPayment> {
        const id = route.params['id'] ? route.params['id'] : null;
        if (id) {
            return this.service.find(id).pipe(
                filter((response: HttpResponse<Payment>) => response.ok),
                map((payment: HttpResponse<Payment>) => payment.body)
            );
        }
        return of(new Payment());
    }
}

export const paymentRoute: Routes = [
    {
        path: '',
        component: PaymentComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'ninjaccountApp.payment.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: ':id/view',
        component: PaymentDetailComponent,
        resolve: {
            payment: PaymentResolve
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'ninjaccountApp.payment.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'new',
        component: PaymentUpdateComponent,
        resolve: {
            payment: PaymentResolve
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'ninjaccountApp.payment.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: ':id/edit',
        component: PaymentUpdateComponent,
        resolve: {
            payment: PaymentResolve
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'ninjaccountApp.payment.home.title'
        },
        canActivate: [UserRouteAccessService]
    }
];

export const paymentPopupRoute: Routes = [
    {
        path: ':id/delete',
        component: PaymentDeletePopupComponent,
        resolve: {
            payment: PaymentResolve
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'ninjaccountApp.payment.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    }
];
