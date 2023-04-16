import Component from '@glimmer/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';

export default class RegisterController extends Component {
  @service router;
  @service('regex-check') regex;

  @tracked isUpiIdValid = true;
  @tracked isMobileNumValid = true;
  @tracked isEmailValid = true;
  @tracked isNameValid = true;
  @tracked isPasswordValid = true;

  @action
  validateFormElements(fieldName, event) {
    if (fieldName === 'upiId') {
      this.isUpiIdValid = this.regex.get('upiRegex').test(event.target.value);
      console.log(this.isUpiIdValid);
    } else if (fieldName === 'mobileNum') {
      this.isMobileNumValid = this.regex
        .get('mobileNumRegex')
        .test(event.target.value);
      console.log(this.isMobileNumValid);
    } else if (fieldName === 'email') {
      this.isEmailValid = this.regex.get('emailRegex').test(event.target.value);
      console.log(this.isEmailValid);
    } else if (fieldName === 'name') {
      this.isNameValid = this.regex.get('nameRegex').test(event.target.value);
      console.log(this.isNameValid);
    } else if (fieldName === 'password') {
      this.isPasswordValid = this.regex
        .get('passwordRegex')
        .test(event.target.value);
      console.log(this.isPasswordValid);
    }
  }

  @action onInvalid(fieldName, event) {
    console.log(fieldName, event);
  }

  @action
  async registerCustomer(event) {
    event.preventDefault();
    console.log(event.target);

    const element = event.target.closest('.form');
    let accountName = element.querySelector('.input-account-name').value;
    let email = element.querySelector('.input-email').value;
    let mobileNum = element.querySelector('.input-mobile-num').value;
    let upiid = element.querySelector('.input-upi').value;
    let password = element.querySelector('.input-password').value;

    console.log(accountName, email, mobileNum, password, upiid);

    if (
      this.isNameValid &&
      this.isEmailValid &&
      this.isMobileNumValid &&
      this.isUpiIdValid &&
      this.isPasswordValid &&
      email != '' &&
      mobileNum != '' &&
      upiid != '' &&
      password != '' &&
      accountName != ''
    ) {
      fetch('http://localhost:8000/register', {
        method: 'POST',
        headers: {
          name: accountName,
          email: email,
          phonenum: mobileNum,
          upiid: upiid,
          password: password,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({}),
      })
        .then((response) => {
          console.log('Response code:', response);
          return response.json();
        })
        .then((data) => {
          console.log(data);
          if (data.code == 200) {
            fetch('http://localhost:8000/accountinfo', {
              method: 'POST',
              headers: {
                upiid: upiid,
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({}),
            })
              .then((response) => {
                console.log('Response:', response);
                return response.json();
              })
              .then((data) => {
                console.log(data);
                const result = `Your Customer ID: ${
                  data.message.split(',')[1].split(':')[1]
                } \nYour Account Number:${
                  data.message.split(',')[0].split(':')[1]
                }
                `;
                alert(result);
                this.router.transitionTo('index');
              });
          } else {
            alert(data.message);
          }
        });
    } else {
      alert('Enter valid credentials');
    }
  }
}
