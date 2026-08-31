const TOKEN_KEY = 'ihub_token'
const ROLE_KEY = 'ihub_role'
const ENVIRONMENT_KEY = 'ihub_environment'
const ENVIRONMENT_NAME_KEY = 'ihub_environment_name'
const PASSWORD_CHANGE_REQUIRED_KEY =
  'ihub_password_change_required'

export function saveAuth({
  token,
  role,
  environment,
  environmentName,
  passwordChangeRequired,
}) {
  sessionStorage.setItem(TOKEN_KEY, token)
  sessionStorage.setItem(ROLE_KEY, role)
  sessionStorage.setItem(ENVIRONMENT_KEY, environment)
  sessionStorage.setItem(
    ENVIRONMENT_NAME_KEY,
    environmentName,
  )

  sessionStorage.setItem(
    PASSWORD_CHANGE_REQUIRED_KEY,
    String(passwordChangeRequired),
  )
}

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function getRole() {
  return sessionStorage.getItem(ROLE_KEY)
}

export function getEnvironment() {
  return sessionStorage.getItem(ENVIRONMENT_KEY)
}

export function getEnvironmentName() {
  return sessionStorage.getItem(
    ENVIRONMENT_NAME_KEY,
  )
}

export function isPasswordChangeRequired() {
  return (
    sessionStorage.getItem(
      PASSWORD_CHANGE_REQUIRED_KEY,
    ) === 'true'
  )
}

export function setPasswordChangeRequired(required) {
  sessionStorage.setItem(
    PASSWORD_CHANGE_REQUIRED_KEY,
    String(required),
  )
}

export function isAuthenticated() {
  return Boolean(getToken())
}

export function clearAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(ROLE_KEY)
  sessionStorage.removeItem(ENVIRONMENT_KEY)
  sessionStorage.removeItem(ENVIRONMENT_NAME_KEY)
  sessionStorage.removeItem(
    PASSWORD_CHANGE_REQUIRED_KEY,
  )
}
