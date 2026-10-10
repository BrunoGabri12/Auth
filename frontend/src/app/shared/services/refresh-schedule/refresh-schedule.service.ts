import { OnDestroy, Service } from '@angular/core';
import { Observable, Subscription, switchMap, timer } from 'rxjs';

const REFRESH_MARGIN_MS = 60_000;


@Service()
export class RefreshScheduleService implements OnDestroy {
    private schedule?: Subscription;


    init(expiresInSeconds: number, onExpire: () => Observable<unknown>) {
        this.removeSchedule();

        const delay = Math.max(0, expiresInSeconds * 1000 - REFRESH_MARGIN_MS);

        this.schedule = timer(delay).pipe(
            switchMap(() => onExpire())
        ).subscribe();
    }


    removeSchedule(){
        this.schedule?.unsubscribe();
        this.schedule = undefined;
    }


    ngOnDestroy(): void {
        this.removeSchedule();
    }
}
