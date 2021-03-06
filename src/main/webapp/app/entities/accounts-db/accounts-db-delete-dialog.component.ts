import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAccountsDB } from 'app/shared/model/accounts-db.model';
import { AccountsDBService } from './accounts-db.service';

@Component({
    selector: 'jhi-accounts-db-delete-dialog',
    templateUrl: './accounts-db-delete-dialog.component.html'
})
export class AccountsDBDeleteDialogComponent {
    accountsDB: IAccountsDB;

    constructor(
        protected accountsDBService: AccountsDBService,
        public activeModal: NgbActiveModal,
        protected eventManager: JhiEventManager
    ) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.accountsDBService.delete(id).subscribe(response => {
            this.eventManager.broadcast({
                name: 'accountsDBListModification',
                content: 'Deleted an accountsDB'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-accounts-db-delete-popup',
    template: ''
})
export class AccountsDBDeletePopupComponent implements OnInit, OnDestroy {
    protected ngbModalRef: NgbModalRef;

    constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

    ngOnInit() {
        this.activatedRoute.data.subscribe(({ accountsDB }) => {
            setTimeout(() => {
                this.ngbModalRef = this.modalService.open(AccountsDBDeleteDialogComponent as Component, {
                    size: 'lg',
                    backdrop: 'static'
                });
                this.ngbModalRef.componentInstance.accountsDB = accountsDB;
                this.ngbModalRef.result.then(
                    result => {
                        this.router.navigate(['/accounts-db', { outlets: { popup: null } }]);
                        this.ngbModalRef = null;
                    },
                    reason => {
                        this.router.navigate(['/accounts-db', { outlets: { popup: null } }]);
                        this.ngbModalRef = null;
                    }
                );
            }, 0);
        });
    }

    ngOnDestroy() {
        this.ngbModalRef = null;
    }
}
