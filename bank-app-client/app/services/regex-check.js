import Service from '@ember/service';
import { action } from '@ember/object';

export default class RegexCheckService extends Service {
  custIdRegex = /^-?\d+$/;
  amountRegex = /^\d+(\.\d+)?$/;
  upiRegex = /^[a-zA-Z0-9._-]{1,40}@okpiggybank$/;
  mobileNumRegex = /^\+?\d{1,3}[-.\s]?\d{3,4}[-.\s]?\d{4}$/;
  emailRegex = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
  nameRegex = /^[a-zA-Z]+(?:[ ]+[a-zA-Z]+)*$/;
  passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+])[a-zA-Z\d!@#$%^&*()_+]{8,}$/;
}
