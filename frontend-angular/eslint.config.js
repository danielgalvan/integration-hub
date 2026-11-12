const parser = require('@typescript-eslint/parser');
const templateParser = require('@angular-eslint/template-parser');
const templatePlugin = require('@angular-eslint/eslint-plugin-template');

module.exports = [{
  files: ['**/*.ts'],
  languageOptions: { parser },
  processor: templatePlugin.processors['extract-inline-html'],
  rules: { 'no-unused-vars': 'error' },
}, {
  files: ['**/*.html'],
  languageOptions: { parser: templateParser },
  plugins: { '@angular-eslint/template': templatePlugin },
  rules: {
    '@angular-eslint/template/eqeqeq': 'error',
    '@angular-eslint/template/no-duplicate-attributes': 'error',
    '@angular-eslint/template/no-negated-async': 'error',
  },
}];
