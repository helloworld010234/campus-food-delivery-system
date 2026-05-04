const fs=require('fs');
const {Resvg}=require('@resvg/resvg-js');
const svg=fs.readFileSync('D:/sky/sky/nginx/html/sky/img/undraw-candidates/on-the-way_zwi3.svg');
const r=new Resvg(svg,{fitTo:{mode:'width',value:1400},background:'rgba(0,0,0,0)'});
const png=r.render().asPng();
fs.writeFileSync('D:/sky/sky/nginx/html/sky/img/undraw-candidates/on-the-way_zwi3_1400.png',png);
console.log('rendered',png.length);
