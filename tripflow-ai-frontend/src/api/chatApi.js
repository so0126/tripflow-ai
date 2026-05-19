import api from './axios'

async function chat(request) {
  const res = await api.post('/chat', request )
  return res.data;
}

async function confirmAction(data) {
  const res = await api.post('/api/chat/confirm-action', data)
  return res.data;
}

export default {
  chat,
  confirmAction
};
