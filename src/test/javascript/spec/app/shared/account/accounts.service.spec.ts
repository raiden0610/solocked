import { SessionStorageService } from 'ngx-webstorage/dist/services';
import { JhiDataUtils } from 'ng-jhipster';
import { NinjaccountTestModule } from './../../../test.module';
import { TestBed, async, fakeAsync, inject } from '@angular/core/testing';
import { AccountsService } from '../../../../../../main/webapp/app/shared/account/accounts.service';
import { AccountsDBService } from '../../../../../../main/webapp/app/entities/accounts-db/accounts-db.service';
import { CryptoUtilsService } from '../../../../../../main/webapp/app/shared/crypto/crypto-utils.service';
import { CryptoService } from '../../../../../../main/webapp/app/shared/crypto/crypto.service';
import { AccountsTechService } from '../../../../../../main/webapp/app/shared/account/accounts-tech.service';
import {TranslateService} from '@ngx-translate/core';
import {MatSnackBar} from '@angular/material';

describe('Services Tests', () => {

    describe('Accounts Service', () => {
        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [NinjaccountTestModule],
                declarations: [],
                providers: [
                    AccountsService,
                    AccountsDBService,
                    CryptoUtilsService,
                    JhiDataUtils,
                    SessionStorageService,
                    CryptoService,
                    AccountsTechService,
                    {
                        provide: TranslateService,
                        useValue: null
                    },
                    {
                        provide: MatSnackBar,
                        useValue: null
                    }
                ]
            }).compileComponents();
        }));

        it('return encrypted db with random authentication key',
            inject([AccountsService],
                fakeAsync((service: AccountsService) => {
                    const accounts = service.init();
                    const accountsDB = accounts.accounts;

                    expect(accounts.authenticationKey.length).toEqual(22);
                    expect(accountsDB.length).toEqual(0);
                })
            )
        );
    });
});
