import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
function* walk(dir) {
  for (const entry of fs.readdirSync(dir,{withFileTypes:true})) {
    const file=path.join(dir,entry.name);
    if(entry.isDirectory()) yield* walk(file); else yield file;
  }
}
const dirs=['common/src/main/resources','common/src/generated','neoforge/src/main/resources'].map(d=>path.join(root,d));
let count=0;
for(const dir of dirs) for(const file of walk(dir)) if(file.endsWith('.json')||file.endsWith('.mcmeta')) {
  JSON.parse(fs.readFileSync(file,'utf8')); count++;
  assert(!fs.readFileSync(file,'utf8').includes('terrain_slabs:'),`Old registry namespace in ${file}`);
}
const profiles=JSON.parse(fs.readFileSync(path.join(root,'compat/profiles.json'),'utf8'));
assert.equal(new Set(profiles.map(p=>p.source)).size,profiles.length,'Duplicate profile');
const generated=path.join(root,'common/src/generated');
for(const p of profiles) {
  const id=p.source.replace(':','/')+'_slab';
  for(const kind of ['recipe','loot_table/blocks']) {
    if(kind==='recipe'&&['farmland','path'].includes(p.kind))continue;
    const data=JSON.parse(fs.readFileSync(path.join(generated,`data/wildslabs/${kind}/${id}.json`),'utf8'));
    assert.equal(data['neoforge:conditions'][0].modid,p.modId??p.source.split(':')[0],'Optional data needs a mod load condition');
  }
  for(const type of ['bottom','top','double']) assert(fs.existsSync(path.join(dirs[0],`assets/wildslabs/models/block/${id}_${type}.json`)));
}
const farmland=JSON.parse(fs.readFileSync(path.join(dirs[0],'assets/wildslabs/blockstates/farmland_slab.json'),'utf8'));
assert.equal(Object.keys(farmland.variants).length,24,'Every orientation/moisture state needs a model');
const metadata=fs.readFileSync(path.join(dirs[2],'META-INF/neoforge.mods.toml'),'utf8');
assert(!/modId\s*=\s*"terrain_slabs"[\s\S]*?type\s*=\s*"required"/.test(metadata),'Original mod must not be required');
console.log(`OK: ${count} JSON resources; ${profiles.length} optional profiles; 24 farmland states; independent mod identity.`);
