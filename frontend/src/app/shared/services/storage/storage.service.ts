import { Service } from '@angular/core';

export interface Property { 
    key:string, 
    value:string,
}

@Service()
export class StorageService {
    private storageProvisorio:Map<string, string> = new Map()

    get(key:string):string | null {
        return this.storageProvisorio.get(key) ?? null;
    }

    setInBatch(prop: Property[]) {
        prop.forEach(p => this.set(p))
    }

    set(property: Property) {
        this.storageProvisorio.set(property.key, property.value);
    }


    clearProperty(key:string) {
        if(!this.storageProvisorio.has(key)) {
            throw new Error("Chave informada não existe");
        }

        this.storageProvisorio.delete(key);
    }

    clearAll() {
        this.storageProvisorio = new Map<string, string>()
    }

}
