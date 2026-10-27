import fs from 'node:fs';
import path from 'node:path';
import {gzipSync} from 'node:zlib';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const str=s=>{const b=Buffer.from(s);const n=Buffer.alloc(2);n.writeUInt16BE(b.length);return Buffer.concat([n,b]);};
const int=n=>{const b=Buffer.alloc(4);b.writeInt32BE(n);return b;};
const tag=(type,name,data)=>Buffer.concat([Buffer.from([type]),str(name),data]);
const nbt=tag(10,'',Buffer.concat([
  tag(3,'DataVersion',int(3955)),
  tag(9,'size',Buffer.concat([Buffer.from([3]),int(3),int(7),int(5),int(7)])),
  tag(9,'palette',Buffer.concat([Buffer.from([10]),int(1),tag(8,'Name',str('minecraft:air')),Buffer.from([0])])),
  tag(9,'blocks',Buffer.concat([Buffer.from([10]),int(0)])),
  tag(9,'entities',Buffer.concat([Buffer.from([10]),int(0)])),
  Buffer.from([0])
]));
const file=path.join(root,'neoforge/src/main/resources/data/wildslabs/structure/test_empty.nbt');
fs.mkdirSync(path.dirname(file),{recursive:true});
fs.writeFileSync(file,gzipSync(nbt));
console.log('Wrote GameTest structure (7 × 5 × 7).');
