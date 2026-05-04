const { spawn } = require('child_process');

const exePath = 'D:/微信web开发者工具/wechatdevtools.exe';
const projectPath = 'D:/sky/sky/project-rjwm-weixin-uniapp-develop-wsy/unpackage/dist/dev/mp-weixin';

const child = spawn(exePath, ['--open', projectPath], {
  detached: true,
  stdio: 'ignore',
  shell: true
});

child.unref();
console.log('WeChat Dev Tools started');
