import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const directory=path.join(root,'build/compat-mods');
const lockFile=path.join(root,'compat/test-mods.lock.json');
fs.mkdirSync(directory,{recursive:true});
const headers={'User-Agent':'WildSlabs/0.1.0 compatibility-tests'};
async function json(url){const response=await fetch(url,{headers});if(!response.ok)throw new Error(`${response.status} ${url}`);return response.json();}
let lock;
const previous=fs.existsSync(lockFile)?JSON.parse(fs.readFileSync(lockFile,'utf8')):null;
if(fs.existsSync(lockFile)&&!process.argv.includes('--refresh'))lock=JSON.parse(fs.readFileSync(lockFile,'utf8'));
else {
  const targets=JSON.parse(fs.readFileSync(path.join(root,'compat/test-targets.json'),'utf8'));
  const entries=new Map(), resolving=new Map();
  async function resolve(project,versionId){
    if(entries.has(project))return entries.get(project).projectId;
    if(resolving.has(project))return resolving.get(project);
    const pending=(async()=>{
      const metadata=await json(`https://api.modrinth.com/v2/project/${project}`);
      if(entries.has(metadata.id))return metadata.id;
      const query=new URLSearchParams({game_versions:JSON.stringify(['1.21.1']),loaders:JSON.stringify(['neoforge'])});
      const version=versionId?await json(`https://api.modrinth.com/v2/version/${versionId}`):(await json(`https://api.modrinth.com/v2/project/${project}/version?${query}`))[0];
      if(!version)throw new Error(`No NeoForge 1.21.1 version: ${metadata.title}`);
      const file=version.files.find(f=>f.primary)??version.files[0];
      const entry={projectId:metadata.id,slug:metadata.slug,title:metadata.title,version:version.version_number,versionId:version.id,filename:file.filename,url:file.url,sha512:file.hashes.sha512,dependencies:[]};
      entries.set(metadata.id,entry);
      const dependencies=version.dependencies.filter(d=>d.dependency_type==='required');
      for(const dependency of dependencies){
        if(!dependency.project_id&&!dependency.version_id)throw new Error(`Unresolved dependency in ${metadata.title}`);
        let projectId=dependency.project_id;
        if(!projectId)projectId=(await json(`https://api.modrinth.com/v2/version/${dependency.version_id}`)).project_id;
        entry.dependencies.push(await resolve(projectId,dependency.version_id));
      }
      return metadata.id;
    })();
    resolving.set(project,pending);
    return pending;
  }
  const roots=[];
  for(const target of targets)roots.push({slug:target,projectId:await resolve(target)});
  lock={minecraft:'1.21.1',loader:'neoforge',roots,mods:[...entries.values()].sort((a,b)=>a.slug.localeCompare(b.slug)),extraArtifacts:previous?.extraArtifacts??[]};
  fs.writeFileSync(lockFile,JSON.stringify(lock,null,2)+'\n');
}
let next=0;
async function worker(){
  while(next<artifacts.length){
    const mod=artifacts[next++];
    const target=path.join(directory,path.basename(mod.filename));
    if(fs.existsSync(target)&&createHash('sha512').update(fs.readFileSync(target)).digest('hex')===mod.sha512){console.log(`Cached ${mod.slug}`);continue;}
    const response=await fetch(mod.url,{headers});if(!response.ok)throw new Error(`Download ${response.status}: ${mod.filename}`);
    const data=Buffer.from(await response.arrayBuffer());
    if(createHash('sha512').update(data).digest('hex')!==mod.sha512)throw new Error(`Hash mismatch: ${mod.filename}`);
    fs.writeFileSync(target,data);
    console.log(`Verified ${mod.slug} ${mod.version}`);
  }
}
const artifacts=[...lock.mods,...lock.extraArtifacts??[]];
await Promise.all(Array.from({length:4},worker));
console.log(`${artifacts.length} verified mods in ${directory}`);
