import { useEffect, useState } from 'react'
import ConfirmDialog from '../components/common/ConfirmDialog'
import TemporaryPasswordDialog from '../components/users/TemporaryPasswordDialog'
import UserForm from '../components/users/UserForm'
import {
  createUser,
  deleteUser,
  getUsers,
  resetUserPassword,
  updateUser,
} from '../services/userService'
import './UsersPage.css'

function UsersPage() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [showForm, setShowForm] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  const [saving, setSaving] = useState(false)

  const [
    temporaryPassword,
    setTemporaryPassword,
  ] = useState(null)

  const [
    userToResetPassword,
    setUserToResetPassword,
  ] = useState(null)

  const [
    userToDelete,
    setUserToDelete,
  ] = useState(null)

  useEffect(() => {
    async function load() {
      try {
        setLoading(true)
        setError(null)

        const data = await getUsers()

        setUsers(data)
      } catch (err) {
        setError(
          err?.message ||
            'Não foi possível carregar os usuários.',
        )
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [])

  async function loadUsers() {
    try {
      setLoading(true)
      setError(null)

      const data = await getUsers()

      setUsers(data)
    } catch (err) {
      setError(
        err?.message ||
          'Não foi possível carregar os usuários.',
      )
    } finally {
      setLoading(false)
    }
  }

  function handleNewUser() {
    setSelectedUser(null)
    setShowForm(true)
    setError(null)
  }

  function handleEditUser(user) {
    setSelectedUser(user)
    setShowForm(true)
    setError(null)
  }

  function handleCloseForm() {
    setSelectedUser(null)
    setShowForm(false)
  }

  async function handleSaveUser(data) {
    try {
      setSaving(true)
      setError(null)

      if (selectedUser) {
        await updateUser(
          selectedUser.id,
          data,
        )
      } else {
        const response =
          await createUser(data)

        setTemporaryPassword(
          response.temporaryPassword,
        )
      }

      await loadUsers()
      handleCloseForm()
    } catch (err) {
      setError(
        err?.message ||
          'Não foi possível salvar o usuário.',
      )
    } finally {
      setSaving(false)
    }
  }

  function handleResetPassword(user) {
    setUserToResetPassword(user)
  }

  function handleCancelResetPassword() {
    setUserToResetPassword(null)
  }

  async function handleConfirmResetPassword() {
    if (!userToResetPassword) {
      return
    }

    try {
      setError(null)

      const response =
        await resetUserPassword(
          userToResetPassword.id,
        )

      setUserToResetPassword(null)

      setTemporaryPassword(
        response.temporaryPassword,
      )

      await loadUsers()
    } catch (err) {
      setUserToResetPassword(null)

      setError(
        err?.message ||
          'Não foi possível resetar a senha.',
      )
    }
  }

  function handleDeleteUser(user) {
    setUserToDelete(user)
  }

  function handleCancelDeleteUser() {
    setUserToDelete(null)
  }

  async function handleConfirmDeleteUser() {
    if (!userToDelete) {
      return
    }

    try {
      setError(null)

      await deleteUser(userToDelete.id)

      setUserToDelete(null)

      await loadUsers()
    } catch (err) {
      setUserToDelete(null)

      setError(
        err?.message ||
          'Não foi possível excluir o usuário.',
      )
    }
  }

  return (
    <section className="users-page">
      <div className="users-page__header">
        <div>
          <p className="users-page__eyebrow">
            Administração
          </p>

          <h1 className="users-page__title">
            Usuários
          </h1>

          <p className="users-page__description">
            Gerencie usuários, perfis de acesso e
            credenciais do Integration Hub.
          </p>
        </div>

        <button
          type="button"
          className="users-page__new-button"
          onClick={handleNewUser}
        >
          + Novo usuário
        </button>
      </div>

      {error && (
        <div
          className="users-page__error"
          role="alert"
        >
          {error}
        </div>
      )}

      {showForm && (
        <UserForm
          key={selectedUser?.id ?? 'new'}
          user={selectedUser}
          saving={saving}
          onSave={handleSaveUser}
          onCancel={handleCloseForm}
        />
      )}

      <div className="users-page__card">
        {loading ? (
          <div className="users-page__state">
            Carregando usuários...
          </div>
        ) : users.length === 0 ? (
          <div className="users-page__state">
            Nenhum usuário cadastrado.
          </div>
        ) : (
          <div className="users-page__table-wrapper">
            <table className="users-page__table">
              <thead>
                <tr>
                  <th>Usuário</th>
                  <th>Nome</th>
                  <th>E-mail</th>
                  <th>Perfil</th>
                  <th>Status</th>
                  <th>Troca de senha</th>
                  <th>Ações</th>
                </tr>
              </thead>

              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <strong>
                        {user.username}
                      </strong>
                    </td>

                    <td>
                      {user.name}
                    </td>

                    <td>
                      {user.email || '—'}
                    </td>

                    <td>
                      <span className="users-page__profile">
                        {getUserTypeLabel(
                          user.type,
                        )}
                      </span>
                    </td>

                    <td>
                      <span
                        className={
                          user.status === 'A'
                            ? 'users-page__status users-page__status--active'
                            : 'users-page__status users-page__status--inactive'
                        }
                      >
                        {user.status === 'A'
                          ? 'Ativo'
                          : 'Inativo'}
                      </span>
                    </td>

                    <td>
                      {user.passwordChangeRequired
                        ? 'Sim'
                        : 'Não'}
                    </td>

                    <td>
                      <div className="users-page__actions">
                        <button
                          type="button"
                          onClick={() =>
                            handleEditUser(user)
                          }
                        >
                          Editar
                        </button>

                        <button
                          type="button"
                          onClick={() =>
                            handleResetPassword(user)
                          }
                        >
                          Resetar senha
                        </button>

                        <button
                          type="button"
                          className="users-page__action--danger"
                          onClick={() =>
                            handleDeleteUser(user)
                          }
                        >
                          Excluir
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmDialog
        open={userToResetPassword !== null}
        title="Resetar senha?"
        message={
          userToResetPassword
            ? `A senha do usuário "${userToResetPassword.username}" será resetada e uma nova senha temporária será gerada.`
            : ''
        }
        confirmLabel="Resetar senha"
        cancelLabel="Cancelar"
        onConfirm={handleConfirmResetPassword}
        onCancel={handleCancelResetPassword}
      />

      <ConfirmDialog
        open={userToDelete !== null}
        title="Excluir usuário?"
        message={
          userToDelete
            ? `O usuário "${userToDelete.username}" será removido permanentemente. Essa ação não pode ser desfeita.`
            : ''
        }
        confirmLabel="Excluir"
        cancelLabel="Cancelar"
        onConfirm={handleConfirmDeleteUser}
        onCancel={handleCancelDeleteUser}
      />

      {temporaryPassword && (
        <TemporaryPasswordDialog
          password={temporaryPassword}
          onClose={() =>
            setTemporaryPassword(null)
          }
        />
      )}
    </section>
  )
}

function getUserTypeLabel(type) {
  switch (type) {
    case 'A':
      return 'Administrador'

    case 'C':
      return 'Criador'

    case 'U':
      return 'Consumidor'

    default:
      return type
  }
}

export default UsersPage
