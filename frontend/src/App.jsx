function App() {
  return (
      <main className="min-h-screen bg-brand-background p-10">
        <section className="mx-auto max-w-5xl rounded-2xl border border-brand-border bg-white p-8 shadow-sm">
          <p className="text-sm font-semibold text-brand-primary">
            RM Bank
          </p>

          <h1 className="mt-2 text-3xl font-bold text-brand-text">
            Admin Portal
          </h1>

          <p className="mt-2 text-brand-muted">
            The React and Tailwind setup is working.
          </p>

          <button
              type="button"
              className="mt-6 rounded-xl bg-brand-primary px-5 py-3 font-semibold text-white hover:opacity-90"
          >
            Continue
          </button>
        </section>
      </main>
  );
}

export default App;