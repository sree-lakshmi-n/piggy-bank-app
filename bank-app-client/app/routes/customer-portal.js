import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class CustomerPortalRoute extends Route {
  @service('auth') auth;
  @service router;

  async model() {
    return [
      'profile',
      'edit-contact',
      'deposit',
      'withdraw',
      'transfer',
      'logout',
    ];
  }
  async afterModel() {
    if (
      this.auth.getCookie('sessionId') === null ||
      this.auth.getCookie('sessionId') === ''
    ) {
      this.router.transitionTo('index');
    } else {
      const sessionId = this.auth.getCookie('sessionId');
      const response = await fetch('http://localhost:8000/custid', {
        method: 'POST',
        headers: {
          sessionid: sessionId,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({}),
      });
      const json = await response.json();
      if (response.ok) {
        console.log(json);
        this.auth.currentCustId = json.message.split('+')[0];
        this.auth.accountNum = json.message.split('+')[1];
        this.auth.upiId = json.message.split('+')[2];
        await this.auth.getCustomerInfo();
        await this.auth.getTransactionTable();
        this.router.transitionTo('customer-portal.profile');
      }
    }
  }
}
