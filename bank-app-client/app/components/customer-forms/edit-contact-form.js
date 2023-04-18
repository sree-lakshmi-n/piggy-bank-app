import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';

export default class CustomerFormsEditContactFormComponent extends Component {
  @service('auth') auth;
  @service router;
  @service('regex-check') regex;

  @tracked isEmailValid = true;
  @tracked isMobileNumValid = true;

  @action
  validateFormElements(fieldName, event) {
    if (fieldName === 'email') {
      this.isEmailValid = this.regex.get('emailRegex').test(event.target.value);
      console.log(this.isEmailValid);
    } else if (fieldName === 'mobileNum') {
      this.isMobileNumValid = this.regex
        .get('mobileNumRegex')
        .test(event.target.value);
      console.log(this.isMobileNumValid);
    }
  }

  @action
  async editContactDetails(event) {
    event.preventDefault();
    const element = event.target.closest('.form');
    let email = element.querySelector('.input-email ').value;
    let mobilenum = element.querySelector('.input-mobile-num').value;
    if (email == '' && mobilenum == '') {
      alert('Enter valid values');
    } else if (this.isEmailValid && this.isMobileNumValid) {
      if (email == '' && mobilenum != '') {
        email = this.auth.get('email');
      } else if (email != '' && mobilenum == '') {
        mobilenum = this.auth.get('phonenum');
      }
      const response = await fetch('http://localhost:8000/update', {
        method: 'POST',
        headers: {
          custid: this.auth.currentCustId,
          email: email,
          phonenum: mobilenum,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({}),
      });
      if (response.ok) {
        const json = await response.json();
        alert(json.message);
        this.auth.email = email;
        this.auth.phonenum = mobilenum;
        element.querySelector('.input-email ').value = '';
        element.querySelector('.input-mobile-num').value = '';
      } else {
        const json = await response.json();
        alert(json.message);
      }
    }
  }
}
