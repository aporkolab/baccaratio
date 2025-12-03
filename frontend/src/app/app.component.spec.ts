import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        AppComponent
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'Baccaratio'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('Baccaratio');
  });

  it('should not render modal by default', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.author-modal')).toBeFalsy();
  });

  it('should render modal when showModal is true', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;
    component.showModal = true;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const modal = compiled.querySelector('.author-modal');
    expect(modal).toBeTruthy();
    expect(modal?.textContent).toContain('Dr. Porkoláb Ádám');
  });

  it('should hide modal on mouseleave', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;
    component.showModal = true;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const modal = compiled.querySelector('.author-modal');
    modal?.dispatchEvent(new Event('mouseleave'));
    fixture.detectChanges();

    expect(component.showModal).toBeFalse();
    expect(compiled.querySelector('.author-modal')).toBeFalsy();
  });

  it('should render author modal links correctly', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;
    component.showModal = true;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    const emailLink = compiled.querySelector('a[href^="mailto:"]') as HTMLAnchorElement;
    expect(emailLink).toBeTruthy();
    expect(emailLink.href).toContain('mailto:adam@porkolab.hu');
    expect(emailLink.target).toBe('_blank');

    const websiteLink = compiled.querySelector('a[href^="https://www.aporkolab.com"]') as HTMLAnchorElement;
    expect(websiteLink).toBeTruthy();
    expect(websiteLink.href).toBe('https://www.aporkolab.com/');
    expect(websiteLink.target).toBe('_blank');

    const githubLink = compiled.querySelector('a[href^="https://github.com/APorkolab"]') as HTMLAnchorElement;
    expect(githubLink).toBeTruthy();
    expect(githubLink.href).toBe('https://github.com/APorkolab');
    expect(githubLink.target).toBe('_blank');
  });
});
