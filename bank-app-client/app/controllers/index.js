import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

export default class IndexController extends Controller {
  @service router;
  @service('auth') auth;
  @service('regex-check') regex;

  @tracked isCustIdValid = true;
  @tracked isPasswordValid = true;

  @action
  validateFormElements(fieldName, event) {
    if (fieldName === 'custid') {
      this.isCustIdValid = this.regex
        .get('custIdRegex')
        .test(event.target.value);
      console.log(this.isCustIdValid);
    } else if (fieldName === 'password') {
      this.isPasswordValid = this.regex
        .get('passwordRegex')
        .test(event.target.value);
      console.log(this.isPasswordValid);
    }
  }

  @action async loginCustomer(event) {
    event.preventDefault();
    console.log(event.target);
    const element = event.target.closest('.form');
    let custId = element.querySelector('.input-cust-id').value;
    let password = element.querySelector('.input-pwd').value;
    console.log(`${custId} ${password}`);
    if (
      this.isCustIdValid &&
      this.isPasswordValid &&
      custId != '' &&
      password != ''
    ) {
      this.auth.login(custId, password);
    } else {
      alert('Enter valid credentials');
    }
  }
}
