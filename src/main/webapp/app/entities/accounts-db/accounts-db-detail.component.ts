import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { JhiEventManager  } from 'ng-jhipster';

import { AccountsDB } from './accounts-db.model';
import { AccountsDBService } from './accounts-db.service';

@Component({
    selector: 'jhi-accounts-db-detail',
    templateUrl: './accounts-db-detail.component.html'
})
export class AccountsDBDetailComponent implements OnInit, OnDestroy {

    accountsDB: AccountsDB;
    private subscription: Subscription;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: JhiEventManager,
        private accountsDBService: AccountsDBService,
        private route: ActivatedRoute
    ) {
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInAccountsDBS();
    }

    load(id) {
        this.accountsDBService.find(id).subscribe((accountsDB) => {
            this.accountsDB = accountsDB;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInAccountsDBS() {
        this.eventSubscriber = this.eventManager.subscribe(
            'accountsDBListModification',
            (response) => this.load(this.accountsDB.id)
        );
    }
}
