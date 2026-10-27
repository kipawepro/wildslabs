import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const dir=path.join(root,'neoforge/run-gametest/mods');
fs.mkdirSync(dir,{recursive:true});
const versions=[];
for (const id of ['biomes-o-plenty','terrablender','glitchcore']) {
  const query=new URLSearchParams({game_versions:JSON.stringify(['1.21.1']),loaders:JSON.stringify(['neoforge'])});
  const response=await fetch(`https://api.modrinth.com/v2/project/${id}/version?${query}`,{headers:{'User-Agent':'WildSlabs/0.1.0 compatibility-validation'}});
  if (!response.ok) throw new Error(`${id}: ${response.status}`);
  const version=(await response.json())[0];
  if (!version) throw new Error(`No Minecraft 1.21.1 NeoForge version of ${id}`);
  const file=version.files.find(f=>f.primary)??version.files[0];
  const download=await fetch(file.url);
  if (!download.ok) throw new Error(`Cannot download ${file.filename}`);
  const data=Buffer.from(await download.arrayBuffer());
  if(createHash('sha512').update(data).digest('hex')!==file.hashes.sha512) throw new Error(`Hash mismatch: ${file.filename}`);
  fs.writeFileSync(path.join(dir,path.basename(file.filename)),data);
  versions.push({project:id,version:version.version_number,file:file.filename,sha512:file.hashes.sha512});
  console.log(`Verified ${file.filename}`);
}
fs.writeFileSync(path.join(root,'build/test-mod-versions.json'),JSON.stringify(versions,null,2));
