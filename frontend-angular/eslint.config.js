const parser = require('@typescript-eslint/parser');

module.exports = [{
  files: ['**/*.ts'],
  languageOptions: { parser },
  rules: { 'no-unused-vars': 'error' },
}];
