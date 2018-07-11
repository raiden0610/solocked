import { Observable } from 'rxjs/Rx';
import { Account } from 'app/shared/account/account.model';
import { Accounts } from 'app/shared/account/accounts.model';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material';
import { TranslateService } from '@ngx-translate/core';
import { AccountsDB } from 'app/shared/model/accounts-db.model';
import { AccountsDBService } from 'app/entities/accounts-db';
import { CryptoService } from 'app/shared/crypto/crypto.service';
import { Version } from 'app/shared/account/version.enum';
import { OperationAccountType } from 'app/shared/account/operation-account-type.enum';
import { SnackComponent } from 'app/shared/snack/snack.component';

@Injectable({ providedIn: 'root' })
export class AccountsService {
    accounts$: BehaviorSubject<Array<Account>>;
    account$: BehaviorSubject<Account>;

    private _dataStore: {
        accounts: Accounts;
    };

    hasToSort: boolean;

    constructor(
        private translateService: TranslateService,
        private snackBar: MatSnackBar,
        //private accountDbService: AccountsDBService,
        private crypto: CryptoService
    ) {
        this._dataStore = { accounts: new Accounts() };

        this.accounts$ = new BehaviorSubject<Array<Account>>(this._dataStore.accounts.accounts);
        this.account$ = new BehaviorSubject<Account>(null);
    }

    getAccount(id: number) {
        if (this._dataStore.accounts.accounts.length === 0) {
            this.accountDbService.synchroDB().subscribe(accountsFromDB => {
                this._dataStore.accounts = accountsFromDB;
                const accounts = this._dataStore.accounts.accounts.filter(account => account.id === id);
                this.account$.next(accounts[0]);
            });
        } else {
            const accounts = this._dataStore.accounts.accounts.filter(account => account.id === id);
            this.account$.next(accounts[0]);
        }
    }

    getAccountsList() {
        if (this._dataStore.accounts.accounts.length === 0) {
            this.accountDbService.synchroDB().subscribe(accountsFromDB => {
                this._dataStore.accounts = accountsFromDB;
                this.sortAccountByName();
                this.accounts$.next(this._dataStore.accounts.accounts);
            });
        } else {
            this.accounts$.next(this._dataStore.accounts.accounts);
        }
    }

    private sortAccountByName() {
        this._dataStore.accounts.accounts.sort((accountA, accountB) => {
            const nameA = accountA.name.toLowerCase();
            const nameB = accountB.name.toLowerCase();
            if (nameA < nameB) {
                // sort string ascending
                return -1;
            }
            if (nameA > nameB) {
                return 1;
            }
            return 0; // default return value (no sorting)
        });
    }

    getAccountsListInstant(): Array<Account> {
        return this._dataStore.accounts.accounts;
    }

    init(): Accounts {
        const accountsInitialized = new Accounts();
        accountsInitialized.version = Version.V1_0;
        accountsInitialized.authenticationKey = this.getRandomString(22);

        return accountsInitialized;
    }

    seqNextVal(accounts: Accounts): number {
        const accountsIds = accounts.accounts.map(account => account.id);
        if (accountsIds.length === 0) {
            return 1;
        } else {
            return Math.max.apply(null, accountsIds) + 1;
        }
    }

    getRandomString(length: number) {
        let text = '';
        const charset = 'abcdefghijklmnopqrstuvwxyz0123456789';

        for (let i = 0; i < length; i++) {
            text += charset.charAt(Math.floor(Math.random() * charset.length));
        }

        return text;
    }

    saveOnBrowser(accounts: Accounts) {
        accounts.operationAccountType = null;
        this._dataStore.accounts = accounts;
        this.accounts$.next(this._dataStore.accounts.accounts);
    }

    saveNewAccount(account: Account | Array<Account>): Observable<AccountsDB> {
        return this.accountDbService.synchroDB().flatMap((accounts: Accounts) => {
            const initVector = this.crypto.getRandomNumber();
            if (account instanceof Account) {
                // Sequence management
                account.id = this.seqNextVal(this._dataStore.accounts);
                // Adding new account
                accounts.accounts.push(account);
            } else {
                for (const newAccount of account) {
                    newAccount.id = this.seqNextVal(this._dataStore.accounts);
                    this._dataStore.accounts.accounts.push(newAccount);
                    this.sortAccountByName();
                    accounts.accounts.push(newAccount);
                }
            }
            this.saveOnBrowser(accounts);
            accounts.operationAccountType = OperationAccountType.CREATE;
            return this.accountDbService.saveEncryptedDB(accounts, initVector);
        });
    }

    updateAccount(accountUpdated: Account) {
        const initVector = this.crypto.getRandomNumber();
        this.accountDbService
            .synchroDB()
            .flatMap((accounts: Accounts) => {
                for (let _i = 0; _i < accounts.accounts.length; _i++) {
                    const account = accounts.accounts[_i];
                    if (account.id === accountUpdated.id) {
                        this.copyAccount(accounts.accounts[_i], accountUpdated);
                        this.copyAccount(this._dataStore.accounts.accounts[_i], accountUpdated);
                    }
                }
                this.saveOnBrowser(accounts);
                accounts.operationAccountType = OperationAccountType.UPDATE;
                return this.accountDbService.saveEncryptedDB(accounts, initVector);
            })
            .subscribe(
                (accountDB: AccountsDB) => {
                    this.sortAccountByName();
                    this.accounts$.next(this._dataStore.accounts.accounts);
                },
                error => {
                    this.errorSnack('ninjaccountApp.accountsDB.update.error');
                }
            );
    }

    clean(): void {
        this._dataStore.accounts = new Accounts();
        this.accounts$.next(this._dataStore.accounts.accounts);
    }

    deleteAccount(accountId: number) {
        const initVector = this.crypto.getRandomNumber();
        this.accountDbService
            .synchroDB()
            .flatMap((accounts: Accounts) => {
                accounts.accounts = accounts.accounts.filter(account => account.id !== accountId);
                this.deleteLocalAccount(accountId);
                accounts.operationAccountType = OperationAccountType.DELETE;
                return this.accountDbService.saveEncryptedDB(accounts, initVector);
            })
            .subscribe(
                (accountDB: AccountsDB) => {
                    this.accounts$.next(this._dataStore.accounts.accounts);
                },
                error => {
                    this.errorSnack('ninjaccountApp.accountsDB.delete.error');
                }
            );
    }

    deleteLocalAccount(accountId: number) {
        this._dataStore.accounts.accounts = this._dataStore.accounts.accounts.filter(account => account.id !== accountId);
        this.saveOnBrowser(this._dataStore.accounts);
    }

    copyAccount(target: Account, source: Account) {
        target.name = source.name;
        target.number = source.number;
        target.username = source.username;
        target.password = source.password;
        target.notes = source.notes;
        target.tags = source.tags;
        target.customs = source.customs;
        target.url = source.url;
        target.payments = source.payments;
    }

    rollingAddedAccount(account: Account): void {
        this.deleteLocalAccount(account.id);
        this.accounts$.next(this._dataStore.accounts.accounts);
    }

    errorSnack(key: string) {
        const message = this.translateService.instant(key);
        const config = new MatSnackBarConfig();
        config.verticalPosition = 'top';
        config.duration = 10000;
        config.data = { icon: 'fa-exclamation-triangle', text: message };
        this.snackBar.openFromComponent(SnackComponent, config);
    }

    resetEntireDB() {
        const initVector = this.crypto.getRandomNumber();
        this.accountDbService
            .synchroDB()
            .flatMap((accounts: Accounts) => {
                accounts.accounts.splice(0, accounts.accounts.length);
                accounts.operationAccountType = OperationAccountType.DELETE_ALL;
                this._dataStore.accounts = accounts;
                return this.accountDbService.saveEncryptedDB(accounts, initVector);
            })
            .subscribe(
                (accountDB: AccountsDB) => {
                    //this.accountDbService.getActualMaxAccount();
                    this.saveOnBrowser(this._dataStore.accounts);
                },
                error => {
                    this.errorSnack('ninjaccountApp.accountsDB.delete.error');
                }
            );
    }
}
