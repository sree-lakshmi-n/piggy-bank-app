import { module, test } from 'qunit';
import { setupRenderingTest } from 'bank-app-client/tests/helpers';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module(
  'Integration | Component | customer-forms/withdraw-form',
  function (hooks) {
    setupRenderingTest(hooks);

    test('it renders', async function (assert) {
      // Set any properties with this.set('myProperty', 'value');
      // Handle any actions with this.set('myAction', function(val) { ... });

      await render(hbs`<CustomerForms::WithdrawForm />`);

      assert.dom(this.element).hasText('');

      // Template block usage:
      await render(hbs`
      <CustomerForms::WithdrawForm>
        template block text
      </CustomerForms::WithdrawForm>
    `);

      assert.dom(this.element).hasText('template block text');
    });
  }
);
