const fs=require('fs');
const path=require('path');
const {Resvg}=require('@resvg/resvg-js');
const dir='D:/sky/sky/nginx/html/sky/img/undraw-candidates';
for(const file of fs.readdirSync(dir)){
  if(!file.endsWith('.svg')) continue;
  const svg=fs.readFileSync(path.join(dir,file));
  const r=new Resvg(svg,{fitTo:{mode:'width',value:900},background:'rgba(0,0,0,0)'});
  const png=r.render().asPng();
  const out=path.join(dir,file.replace('.svg','.png'));
  fs.writeFileSync(out,png);
  console.log('rendered',out,png.length);
}
