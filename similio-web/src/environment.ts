const serverIp = '192.168.0.111';
const port = '8080';

export const environment = {
  production: false,
  serverIp: serverIp,
  serverUrl: `http://${serverIp}:${port}`,
  apiBase: `http://${serverIp}:${port}/api/v1`,
};
