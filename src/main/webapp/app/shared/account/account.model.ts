import {Custom} from './custom-account.model';
import {Payment} from './payment-block.model';

export class Account {
    public id: number;
    public name: string;
    public number: string;
    public username: string;
    public password: string;
    public notes?: string;
    public tags: Array<string>;
    public customs: Array<Custom>;
    public url: string;
    public payments: Array<Payment>;

    constructor(
        username: string,
        password: string,
        name: string,
        id?: number
    ) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.tags = new Array<string>();
        this.customs = new Array<Custom>();
        this.id = id ? id : 0;
        this.payments = new Array<Payment>();
    }
}
