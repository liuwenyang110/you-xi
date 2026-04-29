module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es2021: true,
    'uni-app/global': true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@typescript-eslint/recommended',
    'plugin:prettier/recommended'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    parser: '@typescript-eslint/parser',
    sourceType: 'module'
  },
  plugins: ['vue', '@typescript-eslint'],
  rules: {
    // 自定义规则
    'vue/multi-word-component-names': 'off', // 允许单文件组件名
    '@typescript-eslint/no-explicit-any': 'warn', // 警告any类型
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'prettier/prettier': [
      'error',
      {
        endOfLine: 'auto'
      }
    ]
  },
  overrides: [
    {
      files: ['*.vue'],
      rules: {
        // Vue文件特定规则
      }
    }
  ]
}