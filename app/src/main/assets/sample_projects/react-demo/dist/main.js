(function() {
    console.log('[React Demo] Initializing app...');

    if (typeof React === 'undefined' || typeof ReactDOM === 'undefined') {
        console.error('[React Demo] React or ReactDOM is not defined!');
        return;
    }

    function App() {
        const [todos, setTodos] = React.useState([
            { id: 1, text: '学习 React Hooks', done: true },
            { id: 2, text: '构建 Todo 应用', done: false },
            { id: 3, text: '部署到生产环境', done: false }
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
            React.createElement('h1', null, 'React Todo'),
            React.createElement('p', null, '一个使用 React 18 构建的待办事项应用'),
            React.createElement('div', { className: 'todo-app' },
                React.createElement('div', { className: 'todo-input' },
                    React.createElement('input', {
                        type: 'text',
                        value: input,
                        onChange: (e) => setInput(e.target.value),
                        onKeyPress: handleKeyPress,
                        placeholder: '添加新任务...'
                    }),
                    React.createElement('button', { onClick: addTodo }, '添加')
                ),
                todos.length === 0
                    ? React.createElement('div', { className: 'empty-state' }, '🎉 没有待办事项')
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
                                }, '删除')
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
