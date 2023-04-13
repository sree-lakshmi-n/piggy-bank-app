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
      await this.getCustomerInfo();
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
}
