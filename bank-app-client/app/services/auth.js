import Service from '@ember/service';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class AuthService extends Service {
  @service router;

  isAuthenticated = false;
  currentCustId = null;
  accountNum = null;
  name = null;
  email = null;
  phonenum = null;
  transactions = null;
  maxTransactionAmount = 1000000.0;

  getCookie(name) {
    const cookies = document.cookie.split('; ');
    for (let i = 0; i < cookies.length; i++) {
      const cookie = cookies[i];
      if (cookie.startsWith(`${name}=`)) {
        return cookie.substring(name.length + 1);
      }
    }
    return null;
  }

  session = this.getCookie('sessionId') || null;

  @action async login(custId, password) {
    console.log(custId, password);
    const response = await fetch('http://localhost:8000/login', {
      method: 'POST',
      headers: {
        custid: custId,
        password: password,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
    });

    if (response.ok) {
      const json = await response.json();
      this.isAuthenticated = true;
      console.log(json);
      this.currentCustId = custId;
      this.accountNum = parseInt(json.message.split('+')[1]);
      this.upiId = json.message.split('+')[2];
      console.log(json.message.split('+')[0]);
      let sessionId = json.message.split('+')[3];
      let maxAge = json.message.split('+')[5];
      document.cookie = `sessionId=${sessionId};max-age=${maxAge}; path=/`;
      this.session = sessionId;
      await this.getCustomerInfo();
      await this.getTransactionTable();
      this.router.transitionTo('customer-portal.profile');
    } else {
      alert('Invalid username or password');
    }
  }

  @action async logout() {
    const response = await fetch('http://localhost:8000/logout', {
      method: 'POST',
      headers: {
        custid: this.currentCustId,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
    });

    if (response.ok) {
      const json = await response.json();
      console.log(json);
      this.isAuthenticated = false;
      this.currentCustId = null;
      this.accountNum = null;
      this.upiId = null;
      this.name = null;
      this.email = null;
      this.phonenum = null;
      this.transactions = null;
      document.cookie = 'sessionId=; max-age=0; path=/';
      this.session = null;
      this.router.transitionTo('index');
    } else {
      alert('Invalid session');
      this.router.transitionTo('index');
    }
  }

  @action async getCustomerInfo() {
    console.log('cust info');
    const response = await fetch('http://localhost:8000/customerinfo', {
      method: 'POST',
      headers: {
        custid: this.currentCustId,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
    });
    if (response.ok) {
      const json = await response.json();
      console.log(json);
      let credentials = json.message.split(',');
      this.name = credentials[0].split(':')[1];
      this.email = credentials[1].split(':')[1];
      this.phonenum = credentials[2].split(':')[1];
      console.log(this.name, this.email, this.phonenum);
    } else {
      const json = await response.json();
      console.log(json);
    }
  }
  @action async getTransactionTable() {
    console.log('transaction table');
    const response = await fetch('http://localhost:8000/transactions', {
      method: 'POST',
      headers: {
        accountNum: this.accountNum,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
    });
    if (response.ok) {
      const json = await response.json();
      console.log(json);
      if (json.message != '') {
        this.transactions = JSON.parse(json.message).sort(
          (a, b) => b.transactionId - a.transactionId
        );
      } else {
        this.transactions = '';
      }
    } else {
      const json = await response.json();
      console.log(json);
    }
  }
}
