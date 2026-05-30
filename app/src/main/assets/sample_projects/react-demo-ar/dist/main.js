(function() {
    console.log('[React Demo] Initializing app...');

    if (typeof React === 'undefined' || typeof ReactDOM === 'undefined') {
        console.error('[React Demo] React or ReactDOM is not defined!');
        return;
    }

    function App() {
        const [todos, setTodos] = React.useState([
            { id: 1, text: 'تعلم React Hooks', done: true },
            { id: 2, text: 'بناء تطبيق المهام', done: false },
            { id: 3, text: 'النشر في بيئة الإنتاج', done: false }
        ]);
        const [input, setInput] = React.useState('');

        const addTodo = () => {
            if (input.trim()) {
                setTodos([...todos, { id: Date.now(), text: input, done: false }]);
                setInput('');
            }
        };

        const toggleTodo = (id) => {
            setTodos(todos.map(todo =>
                todo.id === id ? { ...todo, done: !todo.done } : todo
            ));
        };

        const deleteTodo = (id) => {
            setTodos(todos.filter(todo => todo.id !== id));
        };

        const handleKeyPress = (e) => {
            if (e.key === 'Enter') addTodo();
        };

        return React.createElement('div', null,
            React.createElement('svg', { className: 'react-logo', viewBox: '-11.5 -10.23174 23 20.46348' },
                React.createElement('circle', { cx: '0', cy: '0', r: '2.05', fill: '#61dafb' }),
                React.createElement('g', { stroke: '#61dafb', strokeWidth: '1', fill: 'none' },
                    React.createElement('ellipse', { rx: '11', ry: '4.2' }),
                    React.createElement('ellipse', { rx: '11', ry: '4.2', transform: 'rotate(60)' }),
                    React.createElement('ellipse', { rx: '11', ry: '4.2', transform: 'rotate(120)' })
                )
            ),
            React.createElement('h1', null, 'قائمة المهام'),
            React.createElement('p', null, 'تطبيق مهام مبني بـ React 18'),
            React.createElement('div', { className: 'todo-app' },
                React.createElement('div', { className: 'todo-input' },
                    React.createElement('input', {
                        type: 'text',
                        value: input,
                        onChange: (e) => setInput(e.target.value),
                        onKeyPress: handleKeyPress,
                        placeholder: 'أضف مهمة جديدة...'
                    }),
                    React.createElement('button', { onClick: addTodo }, 'إضافة')
                ),
                todos.length === 0
                    ? React.createElement('div', { className: 'empty-state' }, '🎉 لا توجد مهام')
                    : React.createElement('ul', { className: 'todo-list' },
                        todos.map(todo =>
                            React.createElement('li', {
                                key: todo.id,
                                className: 'todo-item ' + (todo.done ? 'done' : '')
                            },
                                React.createElement('input', {
                                    type: 'checkbox',
                                    checked: todo.done,
                                    onChange: () => toggleTodo(todo.id)
                                }),
                                React.createElement('span', null, todo.text),
                                React.createElement('button', {
                                    onClick: () => deleteTodo(todo.id)
                                }, 'حذف')
                            )
                        )
                    )
            )
        );
    }

    try {
        const root = ReactDOM.createRoot(document.getElementById('root'));
        root.render(React.createElement(App));
        console.log('[React Demo] App mounted successfully!');
    } catch (e) {
        console.error('[React Demo] Failed to mount app:', e);
    }
})();
